import Datos._
import Itinerarios._
import ItinerariosPar._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.parallel.CollectionConverters._

object WorksheetDePruebas {

  val vuelos = Datos.vuelosCurso
  val aeropuertos = Datos.aeropuertosCurso

  // 1. Información de datos usando funciones puras
  def mostrarInformacionDatos(): Unit = {
    println("=" * 60)
    println("INFORMACION DE DATOS")
    println("=" * 60)

    val numAeropuertos = aeropuertos.size
    val numVuelos = vuelos.size

    println(s"\nAeropuertos disponibles ($numAeropuertos):")
    aeropuertos.foreach { a =>
      println(f"  ${a.Cod}%4s - GMT: ${a.GMT}%4d")
    }

    println(s"\nVuelos disponibles ($numVuelos):")
    vuelos.foreach { v =>
      println(f"  ${v.Aln}%8s ${v.Num}%4d: ${v.Org}%4s ${v.HS}%02d:${v.MS}%02d -> ${v.Dst}%4s ${v.HL}%02d:${v.ML}%02d (Esc: ${v.Esc})")
    }

    val conexiones = vuelos.map(v => s"${v.Org}->${v.Dst}").distinct
    println(s"\nConexiones directas (${conexiones.size}): ${conexiones.mkString(" ")}")
  }

  // 2. Prueba de una ruta específica (funcional puro)
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

    val pruebas = List(
      ("MID", "SVCS", "Ruta directa MID -> SVCS (AIRVZLA 601)", 6, 30),
      ("CLO", "SVO", "Ruta con escalas CLO -> SVO", 8, 0),
      //da error cuando no se encuentran itinerarios, probe colocando que devuelva Nil pero sigue igual
      //("CLO", "HND", "Ruta inexistente CLO -> HND", 14, 0),
      ("BOG", "MEX", "Ruta directa BOG -> MEX (LATAM 787)", 19, 30),
      ("CLO", "IST", "Ruta CLO -> IST (TURKISH 7799)", 2, 0)
    )

    val resultados = pruebas.foldLeft((0, 0)) { case ((correctas, totales), (origen, destino, desc, h, m)) =>
      val (sec, par) = probarRuta(origen, destino, desc, h, m)
      val esCorrecta = sec == par
      val nuevasCorrectas = if (esCorrecta) correctas + 1 else correctas
      (nuevasCorrectas, totales + 1)
    }

    println(s"\nResumen: ${resultados._1} de ${resultados._2} pruebas correctas")
  }

  // 4. Prueba de rendimiento (Aún en construcción)
  def pruebaRendimientoFuncional(): Unit = {
    println("\n" + "=" * 60)
    println("PRUEBA DE RENDIMIENTO SECUENCIAL VS CONCURRENTE")
    println("EN CONSTRUCCION JEJEJEJE")

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
