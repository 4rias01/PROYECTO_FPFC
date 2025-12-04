import Datos._
import Itinerarios._
import ItinerariosPar._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.parallel.CollectionConverters._
import org.scalameter._


object WorksheetDePruebas {

  val vuelos = Datos.vuelos.take(100)  // Limitar vuelos con take
  val aeropuertos = Datos.aeropuertos  

  // 1. Información de datos usando funciones puras
  def mostrarInformacionDatos(): Unit = {
    println("=" * 60)
    println("INFORMACION DE DATOS")
    println("=" * 60)

    val numAeropuertos = aeropuertos.size
    val numVuelos = vuelos.size

    println(s"\nAeropuertos disponibles ($numAeropuertos):")
    aeropuertos.foreach { a =>
      //activar si se quieren ver los vuelos
      //println(f"  ${a.Cod}%4s - GMT: ${a.GMT}%4d")
    }

    println(s"\nVuelos disponibles ($numVuelos):")
    vuelos.foreach { v =>
      println(f"  ${v.Aln}%8s ${v.Num}%4d: ${v.Org}%4s ${v.HS}%02d:${v.MS}%02d -> ${v.Dst}%4s ${v.HL}%02d:${v.ML}%02d (Esc: ${v.Esc})")
    }

    val conexiones = vuelos.map(v => s"${v.Org}->${v.Dst}").distinct
    println(s"\nConexiones directas (${conexiones.size}): ${conexiones.mkString(" ")}")
  }

  // 2. Prueba de una ruta específica (funcional puro)
  def probarRuta(origen: String, destino: String, descripcion: String): (List[Itinerario], List[Itinerario]) = {
    //activar si se quiere ver los itinerarios que devuelve
    //println(s"\n[Prueba] $descripcion")
    //println(s"         Ruta: $origen -> $destino")

    val resultadoSecuencial = Try(itinerarios(vuelos, aeropuertos)(origen, destino))
    val resultadoParalelo = Try(itinerariosPar(vuelos, aeropuertos)(origen, destino))

    (resultadoSecuencial, resultadoParalelo) match {
      case (Success(sec), Success(par)) =>
        println(s"  Secuencial: ${sec.size} itinerario(s)")
        println(s"  Paralelo:   ${par.size} itinerario(s)")

        if (sec.nonEmpty) {
          sec.zipWithIndex.foreach { case (it, i) =>
            val ruta = it.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")
            println(s"    ${i + 1}. $ruta")
          }
        }

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

    val pruebas = List(
      ("HOU", "MSY", "Ruta directa HOU -> MSY (4X 373)"),
      ("MSY", "HOU", "Ruta con escalas MSY -> HOU (4X 201)"),
      ("MSY", "DFW", "Ruta con escalas MSY -> DFW (4X 213)"),
      ("MSY", "ORD", "Ruta con escalas MSY -> ORD (4X 374)"),
      ("ORD", "RDU", "Ruta con escalas ORD -> RDU (AA 520)"),
    )

    val resultados = pruebas.foldLeft((0, 0)) { case ((correctas, totales), (origen, destino, desc)) =>
      val (sec, par) = probarRuta(origen, destino, desc)
      val esCorrecta = sec == par
      val nuevasCorrectas = if (esCorrecta) correctas + 1 else correctas
      (nuevasCorrectas, totales + 1)
    }

    println(s"\nResumen: ${resultados._1} de ${resultados._2} pruebas correctas")
  }

  // 4. Prueba de rendimiento usando ScalaMeter
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

    // Función para medir el rendimiento de un algoritmo
    def medirTiempo(algoritmo: (String, String) => List[Itinerario], origen: String, destino: String): Double = {
      val tiempo = configScalameter withWarmer new Warmer.Default measure {
        algoritmo(origen, destino)
      }
      tiempo.value
    }

    // Rutas para probar
    val rutas = List(
      ("HOU", "MSY"),
      ("MSY", "HOU"),
      ("MSY", "DFW"),
      ("MSY", "ORD"),
      ("ORD", "RDU"),
    )

    // Ejecutar las pruebas de rendimiento comparando la versión secuencial y paralela
    rutas.foreach { case (origen, destino) =>
      val tiempoSecuencial = medirTiempo(itinerarios(vuelos.take(100), aeropuertos), origen, destino)
      val tiempoParalelo = medirTiempo(itinerariosPar(vuelos.take(100), aeropuertos), origen, destino)
      val speedUp = tiempoSecuencial / tiempoParalelo

      println(s"\nRuta: $origen -> $destino")
      println(s"  Tiempo secuencial: $tiempoSecuencial ms")
      println(s"  Tiempo paralelo:   $tiempoParalelo ms")
      println(s"  Speed-up:          $speedUp")
    }

    println("=" * 60)
  }

  // 5. Análisis de datos usando operaciones funcionales
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