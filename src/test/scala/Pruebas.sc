import Datos._
import Itinerarios._
import ItinerariosPar._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.parallel.CollectionConverters._
import org.scalameter._

object WorksheetCompleto {

  val vuelos = Datos.vuelos.take(50)
  val aeropuertos = Datos.aeropuertos

  // Cache para itinerarios
  private var _itinerariosCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosParCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosTiempoCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosTiempoParCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosAireCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosAireParCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosEscalasCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerariosEscalasParCache: Option[(String, String) => List[Itinerario]] = None
  private var _itinerarioSalidaCache: Option[(String, String, Int, Int) => Itinerario] = None
  private var _itinerarioSalidaParCache: Option[(String, String, Int, Int) => Itinerario] = None

  // Inicializar caches
  def initCaches(): Unit = {
    println("Inicializando caches...")
    _itinerariosCache = Some(itinerarios(vuelos, aeropuertos))
    _itinerariosParCache = Some(itinerariosPar(vuelos, aeropuertos))
    _itinerariosTiempoCache = Some(itinerariosTiempo(vuelos, aeropuertos))
    _itinerariosTiempoParCache = Some(itinerariosTiempoPar(vuelos, aeropuertos))
    _itinerariosAireCache = Some(itinerariosAire(vuelos, aeropuertos))
    _itinerariosAireParCache = Some(itinerariosAirePar(vuelos, aeropuertos))
    _itinerariosEscalasCache = Some(itinerariosEscalas(vuelos, aeropuertos))
    _itinerariosEscalasParCache = Some(itinerariosEscalasPar(vuelos, aeropuertos))
    _itinerarioSalidaCache = Some(itinerarioSalida(vuelos, aeropuertos))
    _itinerarioSalidaParCache = Some(itinerarioSalidaPar(vuelos, aeropuertos))
    println("Caches listos")
  }

  // Getters para los caches
  def itinerariosCache: (String, String) => List[Itinerario] =
    _itinerariosCache.getOrElse(itinerarios(vuelos, aeropuertos))

  def itinerariosParCache: (String, String) => List[Itinerario] =
    _itinerariosParCache.getOrElse(itinerariosPar(vuelos, aeropuertos))

  def itinerariosTiempoCache: (String, String) => List[Itinerario] =
    _itinerariosTiempoCache.getOrElse(itinerariosTiempo(vuelos, aeropuertos))

  def itinerariosTiempoParCache: (String, String) => List[Itinerario] =
    _itinerariosTiempoParCache.getOrElse(itinerariosTiempoPar(vuelos, aeropuertos))

  def itinerariosAireCache: (String, String) => List[Itinerario] =
    _itinerariosAireCache.getOrElse(itinerariosAire(vuelos, aeropuertos))

  def itinerariosAireParCache: (String, String) => List[Itinerario] =
    _itinerariosAireParCache.getOrElse(itinerariosAirePar(vuelos, aeropuertos))

  def itinerariosEscalasCache: (String, String) => List[Itinerario] =
    _itinerariosEscalasCache.getOrElse(itinerariosEscalas(vuelos, aeropuertos))

  def itinerariosEscalasParCache: (String, String) => List[Itinerario] =
    _itinerariosEscalasParCache.getOrElse(itinerariosEscalasPar(vuelos, aeropuertos))

  def itinerarioSalidaCache: (String, String, Int, Int) => Itinerario =
    _itinerarioSalidaCache.getOrElse(itinerarioSalida(vuelos, aeropuertos))

  def itinerarioSalidaParCache: (String, String, Int, Int) => Itinerario =
    _itinerarioSalidaParCache.getOrElse(itinerarioSalidaPar(vuelos, aeropuertos))

  // ==================== 1. INFORMACIÓN DE DATOS ====================
  def mostrarInformacionDatos(): Unit = {
    println("\n" + "=" * 60)
    println("INFORMACION DE DATOS")
    println("=" * 60)

    val numAeropuertos = aeropuertos.size
    val numVuelos = vuelos.size
    val conexiones = vuelos.map(v => s"${v.Org}->${v.Dst}").distinct.size

    println(s"Aeropuertos: $numAeropuertos")
    println(s"Vuelos: $numVuelos (limitados a 200)")
    println(s"Conexiones directas: $conexiones")
  }

  // ==================== 2. PRUEBAS DE CORRECCIÓN ====================

  def probarRutaBasica(origen: String, destino: String, descripcion: String): (List[Itinerario], List[Itinerario]) = {
    println(s"\n[Basico] $descripcion")
    println(s"Ruta: $origen -> $destino")

    val sec = Try(itinerariosCache(origen, destino)).getOrElse(Nil)
    val par = Try(itinerariosParCache(origen, destino)).getOrElse(Nil)

    println(s"Secuencial: ${sec.size} itinerarios")
    println(s"Paralelo:   ${par.size} itinerarios")

    if (sec == par) {
      println("OK - Coinciden")
      (sec, par)
    } else {
      println("ERROR - No coinciden")
      (Nil, Nil)
    }
  }

  def probarRutaTiempo(origen: String, destino: String, descripcion: String): (List[Itinerario], List[Itinerario]) = {
    println(s"\n[Tiempo] $descripcion")
    println(s"Ruta: $origen -> $destino")

    val sec = Try(itinerariosTiempoCache(origen, destino)).getOrElse(Nil)
    val par = Try(itinerariosTiempoParCache(origen, destino)).getOrElse(Nil)

    println(s"Secuencial: ${sec.size} itinerarios")
    println(s"Paralelo:   ${par.size} itinerarios")

    if (sec == par) {
      println("OK - Coinciden")
      (sec, par)
    } else {
      println("ERROR - No coinciden")
      (Nil, Nil)
    }
  }

  def probarRutaAire(origen: String, destino: String, descripcion: String): (List[Itinerario], List[Itinerario]) = {
    println(s"\n[Aire] $descripcion")
    println(s"Ruta: $origen -> $destino")

    val sec = Try(itinerariosAireCache(origen, destino)).getOrElse(Nil)
    val par = Try(itinerariosAireParCache(origen, destino)).getOrElse(Nil)

    println(s"Secuencial: ${sec.size} itinerarios")
    println(s"Paralelo:   ${par.size} itinerarios")

    if (sec == par) {
      println("OK - Coinciden")
      (sec, par)
    } else {
      println("ERROR - No coinciden")
      (Nil, Nil)
    }
  }

  def probarRutaEscalas(origen: String, destino: String, descripcion: String): (List[Itinerario], List[Itinerario]) = {
    println(s"\n[Escalas] $descripcion")
    println(s"Ruta: $origen -> $destino")

    val sec = Try(itinerariosEscalasCache(origen, destino)).getOrElse(Nil)
    val par = Try(itinerariosEscalasParCache(origen, destino)).getOrElse(Nil)

    println(s"Secuencial: ${sec.size} itinerarios")
    println(s"Paralelo:   ${par.size} itinerarios")

    if (sec == par) {
      println("OK - Coinciden")
      (sec, par)
    } else {
      println("ERROR - No coinciden")
      (Nil, Nil)
    }
  }

  def probarRutaSalida(origen: String, destino: String, descripcion: String, h: Int, m: Int): (Itinerario, Itinerario) = {
    println(s"\n[Salida] $descripcion")
    println(s"Ruta: $origen -> $destino a las ${h}:${m.toString.padTo(2, '0')}")

    val sec = Try(itinerarioSalidaCache(origen, destino, h, m)).getOrElse(Nil)
    val par = Try(itinerarioSalidaParCache(origen, destino, h, m)).getOrElse(Nil)

    if (sec.nonEmpty) {
      println(s"Secuencial: ${sec.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")}")
      println(s"Paralelo:   ${par.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")}")

      if (sec == par) {
        println("OK - Coinciden")
        (sec, par)
      } else {
        println("ERROR - No coinciden")
        (Nil, Nil)
      }
    } else {
      println("No se encontro itinerario")
      (Nil, Nil)
    }
  }

  def ejecutarPruebasCorreccion(): Unit = {
    println("\n" + "=" * 60)
    println("PRUEBAS DE CORRECCION")
    println("=" * 60)

    val rutasComunes = List(
      ("HOU", "MSY", "Ruta directa HOU->MSY"),
      ("HOU", "DFW", "Ruta 1 escala HOU->DFW"),
      ("SEA", "MIA", "Ruta larga SEA->MIA"),
      ("ABQ", "TPA", "Ruta sin conexion directa"),
      ("MSY", "ORD", "Multiples conexiones MSY->ORD")
    )

    val rutasSalida = List(
      ("HOU", "MSY", "Ruta directa HOU->MSY", 6, 0),
      ("HOU", "DFW", "Ruta con escalas HOU->DFW", 8, 0),
      ("SEA", "MIA", "Ruta larga SEA->MIA", 9, 0),
      ("MSY", "ORD", "Ruta MSY->ORD", 10, 30),
      ("DFW", "SEA", "Ruta DFW->SEA", 14, 0)
    )

    var totalCorrectas = 0
    var totalPruebas = 0

    println("\n--- Itinerarios Basicos ---")
    rutasComunes.foreach { case (origen, destino, desc) =>
      val (sec, par) = probarRutaBasica(origen, destino, desc)
      if (sec == par) totalCorrectas += 1
      totalPruebas += 1
    }

    println("\n--- Itinerarios por Tiempo ---")
    rutasComunes.foreach { case (origen, destino, desc) =>
      val (sec, par) = probarRutaTiempo(origen, destino, desc)
      if (sec == par) totalCorrectas += 1
      totalPruebas += 1
    }

    println("\n--- Itinerarios por Tiempo en Aire ---")
    rutasComunes.foreach { case (origen, destino, desc) =>
      val (sec, par) = probarRutaAire(origen, destino, desc)
      if (sec == par) totalCorrectas += 1
      totalPruebas += 1
    }

    println("\n--- Itinerarios por Escalas ---")
    rutasComunes.foreach { case (origen, destino, desc) =>
      val (sec, par) = probarRutaEscalas(origen, destino, desc)
      if (sec == par) totalCorrectas += 1
      totalPruebas += 1
    }

    println("\n--- Itinerarios por Salida ---")
    rutasSalida.foreach { case (origen, destino, desc, h, m) =>
      val (sec, par) = probarRutaSalida(origen, destino, desc, h, m)
      if (sec == par) totalCorrectas += 1
      totalPruebas += 1
    }

    println(s"\nResumen: $totalCorrectas de $totalPruebas pruebas correctas")
  }

  // ==================== 3. PRUEBAS DE RENDIMIENTO ====================

  def pruebaRendimientoFuncional(): Unit = {
    println("\n" + "=" * 60)
    println("PRUEBAS DE RENDIMIENTO")
    println("=" * 60)

    // Configuración optimizada para 200 vuelos
    val configScalameter = config(
      KeyValue(Key.exec.minWarmupRuns -> 40),
      KeyValue(Key.exec.maxWarmupRuns -> 80),
      KeyValue(Key.exec.benchRuns -> 30),
      KeyValue(Key.exec.independentSamples -> 3),
      KeyValue(Key.verbose -> false)
    )

    def medirTiempo2(algoritmo: (String, String) => List[Itinerario], origen: String, destino: String): Double = {
      val tiempo = configScalameter withWarmer new Warmer.Default measure {
        algoritmo(origen, destino)
      }
      tiempo.value
    }

    def medirTiempo4(algoritmo: (String, String, Int, Int) => Itinerario, origen: String, destino: String, h: Int, m: Int): Double = {
      val tiempo = configScalameter withWarmer new Warmer.Default measure {
        algoritmo(origen, destino, h, m)
      }
      tiempo.value
    }

    val rutas2Param = List(
      ("HOU", "MSY"),
      ("HOU", "DFW"),
      ("SEA", "MIA"),
      ("ABQ", "TPA"),
      ("MSY", "ORD")
    )

    val rutasSalida = List(
      ("HOU", "MSY", 6, 0),
      ("HOU", "DFW", 8, 0),
      ("SEA", "MIA", 9, 0),
      ("DFW", "SEA", 14, 0),
      ("MSY", "ORD", 10, 30)
    )

    val resultados = scala.collection.mutable.Map[String, List[Double]]()

    // Itinerarios Basicos
    println("\n--- Itinerarios Basicos ---")
    rutas2Param.foreach { case (origen, destino) =>
      val tiempoSec = medirTiempo2(itinerariosCache, origen, destino)
      val tiempoPar = medirTiempo2(itinerariosParCache, origen, destino)
      val speedUp = if (tiempoPar > 0) tiempoSec / tiempoPar else 0.0
      resultados("Basicos") = resultados.getOrElse("Basicos", List()) :+ speedUp
      println(s"$origen->$destino: Sec=${tiempoSec}ms, Par=${tiempoPar}ms, Speed-up=${speedUp}")
    }

    // Itinerarios Tiempo
    println("\n--- Itinerarios por Tiempo ---")
    rutas2Param.foreach { case (origen, destino) =>
      val tiempoSec = medirTiempo2(itinerariosTiempoCache, origen, destino)
      val tiempoPar = medirTiempo2(itinerariosTiempoParCache, origen, destino)
      val speedUp = if (tiempoPar > 0) tiempoSec / tiempoPar else 0.0
      resultados("Tiempo") = resultados.getOrElse("Tiempo", List()) :+ speedUp
      println(s"$origen->$destino: Sec=${tiempoSec}ms, Par=${tiempoPar}ms, Speed-up=${speedUp}")
    }

    // Itinerarios Aire
    println("\n--- Itinerarios por Tiempo en Aire ---")
    rutas2Param.foreach { case (origen, destino) =>
      val tiempoSec = medirTiempo2(itinerariosAireCache, origen, destino)
      val tiempoPar = medirTiempo2(itinerariosAireParCache, origen, destino)
      val speedUp = if (tiempoPar > 0) tiempoSec / tiempoPar else 0.0
      resultados("Aire") = resultados.getOrElse("Aire", List()) :+ speedUp
      println(s"$origen->$destino: Sec=${tiempoSec}ms, Par=${tiempoPar}ms, Speed-up=${speedUp}")
    }

    // Itinerarios Escalas
    println("\n--- Itinerarios por Escalas ---")
    rutas2Param.foreach { case (origen, destino) =>
      val tiempoSec = medirTiempo2(itinerariosEscalasCache, origen, destino)
      val tiempoPar = medirTiempo2(itinerariosEscalasParCache, origen, destino)
      val speedUp = if (tiempoPar > 0) tiempoSec / tiempoPar else 0.0
      resultados("Escalas") = resultados.getOrElse("Escalas", List()) :+ speedUp
      println(s"$origen->$destino: Sec=${tiempoSec}ms, Par=${tiempoPar}ms, Speed-up=${speedUp}")
    }

    // Itinerarios Salida
    println("\n--- Itinerarios por Salida ---")
    rutasSalida.foreach { case (origen, destino, h, m) =>
      val tiempoSec = medirTiempo4(itinerarioSalidaCache, origen, destino, h, m)
      val tiempoPar = medirTiempo4(itinerarioSalidaParCache, origen, destino, h, m)
      val speedUp = if (tiempoPar > 0) tiempoSec / tiempoPar else 0.0
      resultados("Salida") = resultados.getOrElse("Salida", List()) :+ speedUp
      println(s"$origen->$destino (${h}:${m}): Sec=${tiempoSec}ms, Par=${tiempoPar}ms, Speed-up=${speedUp}")
    }

    // Resumen estadístico
    println("\n" + "=" * 60)
    println("RESUMEN DE SPEED-UPS")
    println("=" * 60)

    resultados.foreach { case (algoritmo, speedUps) =>
      if (speedUps.nonEmpty) {
        val promedio = speedUps.sum / speedUps.length
        val maximo = speedUps.max
        val minimo = speedUps.min
        println(s"$algoritmo: Promedio=$promedio, Min=$minimo, Max=$maximo")
      }
    }
  }

  // ==================== 4. ANÁLISIS DE DATOS ====================

  def analisisDatosFuncional(): Unit = {
    println("\n" + "=" * 60)
    println("ANALISIS DE DATOS")
    println("=" * 60)

    // Aerolíneas
    val aerolineas = vuelos.groupBy(_.Aln).mapValues(_.size).toList.sortBy(-_._2)
    println(s"\nAerolíneas con más vuelos (top 5):")
    aerolineas.take(5).foreach { case (aerolinea, cantidad) =>
      println(s"  $aerolinea: $cantidad vuelos")
    }

    // Conexiones
    val conexiones = vuelos.groupBy(_.Org).mapValues(_.map(_.Dst).distinct.size).toList.sortBy(-_._2)
    println(s"\nAeropuertos con más conexiones (top 5):")
    conexiones.take(5).foreach { case (aeropuerto, num) =>
      println(s"  $aeropuerto: $num conexiones")
    }

    // Escalas
    val vuelosConEscalas = vuelos.count(_.Esc > 0)
    val porcentaje = (vuelosConEscalas.toDouble / vuelos.size) * 100
    println(s"\nEstadísticas de escalas:")
    println(s"  Vuelos con escalas: $vuelosConEscalas (${porcentaje}%)")
    println(s"  Vuelos directos: ${vuelos.size - vuelosConEscalas}")
  }

  // ==================== 5. EJECUCIÓN PRINCIPAL ====================

  def ejecutarTodasPruebas(): Unit = {
    Try {
      println("\n" + "=" * 60)
      println("INICIANDO WORKSHEET COMPLETO")
      println("Proyecto: Itinerarios FPFC 2025-II")
      println("=" * 60)

      // Inicializar caches
      initCaches()

      // Información de datos
      mostrarInformacionDatos()

      // Pruebas de corrección
      ejecutarPruebasCorreccion()

      // Análisis de datos
      analisisDatosFuncional()

      // Pruebas de rendimiento
      pruebaRendimientoFuncional()

      println("\n" + "=" * 60)
      println("PRUEBAS COMPLETADAS")
      println("=" * 60)

    } match {
      case Success(_) =>
        println("Ejecucion exitosa")

      case Failure(e) =>
        println(s"Error: ${e.getMessage}")
    }
  }
}

// ==============================================
// EJECUCIÓN
// ==============================================

println("\nINICIANDO WORKSHEET")
println()

WorksheetCompleto.ejecutarTodasPruebas()

println("\nFIN DEL WORKSHEET")