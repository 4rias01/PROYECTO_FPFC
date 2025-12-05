import Datos._
import Itinerarios._
import ItinerariosPar._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.parallel.CollectionConverters._
import org.scalameter._

object WorksheetDePruebas {

  val vuelos = Datos.vuelos.take(200)
  val aeropuertos = Datos.aeropuertos

  // 1. Información de datos usando funciones puras
  def mostrarInformacionDatos(): Unit = {
    
    val numAeropuertos = aeropuertos.size
    val numVuelos = vuelos.size

    println(s"\nAeropuertos disponibles ($numAeropuertos):")
    aeropuertos.foreach { a =>
      println(f"  ${a.Cod}%4s - GMT: ${a.GMT}%4d")
    }

    println(s"\nVuelos disponibles ($numVuelos):")
    vuelos.foreach { v =>
      //println(f"  ${v.Aln}%8s ${v.Num}%4d: ${v.Org}%4s ${v.HS}%02d:${v.MS}%02d -> ${v.Dst}%4s ${v.HL}%02d:${v.ML}%02d (Esc: ${v.Esc})")
    }

    val conexiones = vuelos.map(v => s"${v.Org}->${v.Dst}").distinct
    //println(s"\nConexiones directas (${conexiones.size}): ${conexiones.mkString(" ")}")
  }

  // 2. Prueba de una ruta específica 
  def probarRuta(origen: String, destino: String, descripcion: String, h: Int, m: Int): (Itinerario, Itinerario) = {
    println(s"\n[Prueba] $descripcion")
    println(s"         Ruta: $origen -> $destino")

    val resultadoSecuencial = Try(itinerarioSalida(vuelos, aeropuertos)(origen, destino, h, m))
    val resultadoParalelo = Try(itinerarioSalidaPar(vuelos, aeropuertos)(origen, destino, h, m))

    (resultadoSecuencial, resultadoParalelo) match {
      case (Success(sec), Success(par)) =>
        println(s"  Secuencial: ${sec.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")}")
        println(s"  Paralelo:   ${par.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")}")

        if (sec == par) {
          println("  RESULTADO: Correcto - algoritmos coinciden")
        } else {
          println("  RESULTADO: Error - algoritmos difieren")
        }
        (sec, par)

      case (Failure(e), _) =>
        println(s"  ERROR en secuencial: ${e.getMessage}")
        (Nil, Nil)

      case (_, Failure(e)) =>
        println(s"  ERROR en paralelo: ${e.getMessage}")
        (Nil, Nil)
    }
  }

  // 3. Ejecutar todas las pruebas usando fold
  def ejecutarPruebasCorreccion(): Unit = {
    println("\n" + "=" * 60)
    println("PRUEBAS DE CORRECCION")
    println("=" * 60)

    // Actualicé las rutas para usar aeropuertos del conjunto Datos.vuelos
    val pruebas = List(
      ("HOU", "MSY", "Ruta directa HOU -> MSY", 6, 0),
      ("HOU", "DFW", "Ruta con escalas HOU -> DFW", 8, 0),
      ("SEA", "MIA", "Ruta larga SEA -> MIA", 9, 0),
      ("MSY", "ORD", "Ruta MSY -> ORD", 10, 30),
      ("DFW", "SEA", "Ruta DFW -> SEA", 14, 0)
    )

    val resultados = pruebas.foldLeft((0, 0)) { case ((correctas, totales), (origen, destino, desc, h, m)) =>
      val (sec, par) = probarRuta(origen, destino, desc, h, m)
      val esCorrecta = sec == par
      val nuevasCorrectas = if (esCorrecta) correctas + 1 else correctas
      (nuevasCorrectas, totales + 1)
    }

    println(s"\nResumen: ${resultados._1} de ${resultados._2} pruebas correctas")
  }

  // 4. Prueba de rendimiento con ScalaMeter
  def pruebaRendimientoFuncional(): Unit = {
    println("\n" + "=" * 60)
    println("PRUEBA DE RENDIMIENTO SECUENCIAL VS CONCURRENTE")
    println("=" * 60)

    // ScalaMeter: configuración de las mediciones
    val configScalameter = config(
      KeyValue(Key.exec.minWarmupRuns -> 20),
      KeyValue(Key.exec.maxWarmupRuns -> 60),
      KeyValue(Key.verbose -> false)
    )

    // Función para medir el rendimiento de un algoritmo (adaptada para 4 parámetros)
    def medirTiempo(algoritmo: (String, String, Int, Int) => Itinerario, origen: String, destino: String, h: Int, m: Int): Double = {
      val tiempo = configScalameter withWarmer new Warmer.Default measure {
        algoritmo(origen, destino, h, m)
      }
      tiempo.value
    }

    // Rutas para probar scalameter (origen, destino, hora, minuto) - usando aeropuertos del conjunto Datos.vuelos
    val rutas = List(
      ("HOU", "MSY", 6, 0),
      ("HOU", "DFW", 8, 0),
      ("SEA", "MIA", 9, 0),
      ("DFW", "SEA", 14, 0),
      ("MSY", "ORD", 10, 30)
    )

    println("\nComparación de rendimiento para itinerarioSalida:")
    println("(Mejor itinerario por diferencia con hora de cita)")
    println("-" * 50)

    // Ejecutar las pruebas de rendimiento comparando la versión secuencial y paralela
    rutas.foreach { case (origen, destino, h, m) =>
      val tiempoSecuencial = medirTiempo(itinerarioSalida(vuelos, aeropuertos), origen, destino, h, m)
      val tiempoParalelo = medirTiempo(itinerarioSalidaPar(vuelos, aeropuertos), origen, destino, h, m)
      val speedUp = tiempoSecuencial / tiempoParalelo

      println(s"\nRuta: $origen -> $destino (Hora cita: ${h}:${m.toString.padTo(2, '0')})")
      println(f"  Tiempo secuencial: $tiempoSecuencial%8.2f ms")
      println(f"  Tiempo paralelo:   $tiempoParalelo%8.2f ms")
      println(f"  Speed-up:          $speedUp%8.2f x")

      // Información adicional sobre los resultados
      val resultadoSec = Try(itinerarioSalida(vuelos, aeropuertos)(origen, destino, h, m)).getOrElse(Nil)
      if (resultadoSec.nonEmpty) {
        println(s"  Itinerario encontrado: ${resultadoSec.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")}")
        println(s"  Número de vuelos: ${resultadoSec.size}")
      } else {
        println(s"  No se encontró itinerario")
      }
    }

    // Análisis estadístico de speed-ups
    val speedUps = rutas.map { case (origen, destino, h, m) =>
      val tiempoSec = medirTiempo(itinerarioSalida(vuelos, aeropuertos), origen, destino, h, m)
      val tiempoPar = medirTiempo(itinerarioSalidaPar(vuelos, aeropuertos), origen, destino, h, m)
      tiempoSec / tiempoPar
    }

    if (speedUps.nonEmpty) {
      val promedioSpeedUp = speedUps.sum / speedUps.length
      val maxSpeedUp = speedUps.max
      val minSpeedUp = speedUps.min

      println("\n" + "=" * 50)
      println("RESUMEN ESTADÍSTICO DE SPEED-UPS")
      println("=" * 50)
      println(f"  Speed-up promedio: $promedioSpeedUp%.2f x")
      println(f"  Speed-up máximo:   $maxSpeedUp%.2f x")
      println(f"  Speed-up mínimo:   $minSpeedUp%.2f x")
    }
  }

  // 5. Análisis de datos de entrada
  def analisisDatosFuncional(): Unit = {
    println("\n" + "=" * 60)
    println("ANALISIS DE DATOS DE ENTRADA")
    println("=" * 60)

    // Estadísticas usando funciones de orden superior
    val numVuelosPorAerolinea = vuelos
      .groupBy(_.Aln)
      .view
      .mapValues(_.size)
      .toList
      .sortBy(-_._2)

    println("\nVuelos por aerolínea:")
    numVuelosPorAerolinea.foreach { case (aerolinea, cantidad) =>
      println(f"  $aerolinea%10s: $cantidad%2d vuelos")
    }

    val conexionesSalientes = vuelos
      .groupBy(_.Org)
      .view
      .mapValues(_.map(_.Dst).distinct.size)
      .toList
      .sortBy(-_._2)

    println("\nConexiones salientes por aeropuerto:")
    conexionesSalientes.foreach { case (aeropuerto, conexiones) =>
      println(f"  $aeropuerto%4s: $conexiones%2d conexiones directas")
    }
  }

  // 6. Ejecución principal funcional
  def ejecutarTodasPruebas(): Unit = {
    Try {
      mostrarInformacionDatos()
      ejecutarPruebasCorreccion()
      analisisDatosFuncional()
      pruebaRendimientoFuncional()

      println("\n" + "=" * 60)
      println("PRUEBAS COMPLETADAS")
      println("=" * 60)

    } match {
      case Success(_) =>
        println("Ejecucion exitosa")

      case Failure(e) =>
        println(s"Error durante la ejecucion: ${e.getMessage}")
    }
  }
}

// ==============================================
// EJECUCION DEL WORKSHEET
// ==============================================

println("INICIANDO WORKSHEET DE PRUEBAS")
println("Proyecto: Itinerarios FPFC 2025-II")
println()

WorksheetDePruebas.ejecutarTodasPruebas()

println("\nFIN DEL WORKSHEET")