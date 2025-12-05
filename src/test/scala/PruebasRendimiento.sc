import Datos._
import Itinerarios._
import ItinerariosPar._
import org.scalameter._

// Funciones auxiliares para trabajar con Quantity[Double]
def toDouble(t: org.scalameter.Quantity[Double]): Double = t.value
def speedUp(sec: org.scalameter.Quantity[Double], par: org.scalameter.Quantity[Double]): Double = {
  val parValue = toDouble(par)
  if (parValue > 0.0) toDouble(sec) / parValue else 0.0
}

// Configuración para medir tiempos
def tiempoDe[T](body: => T) = {
  val timeA1 = config(
    KeyValue(Key.exec.minWarmupRuns -> 30),
    KeyValue(Key.exec.maxWarmupRuns -> 60),
    KeyValue(Key.verbose -> false)
  ) withWarmer(new Warmer.Default) measure (body)
  timeA1
}

// Crear los objetos de funciones una sola vez para reutilizar
//si se quiere usar los 200 vuelos quitar el comentario de abajo
val vuelos200C = vuelosC1 //++ vuelosC2
val tamañoVuelos200C = vuelos200C.size
println(s"El tamaño de vuelos200C es: $tamañoVuelos200C elementos")

val its200C = itinerarios(vuelos200C, aeropuertos)
val itsPar200C = itinerariosPar(vuelos200C, aeropuertos)

val itsTpo200C = itinerariosTiempo(vuelos200C, aeropuertos)
val itsTpoPar200C = itinerariosTiempoPar(vuelos200C, aeropuertos)

val itsEsc200C = itinerariosEscalas(vuelos200C, aeropuertos)
val itsEscPar200C = itinerariosEscalasPar(vuelos200C, aeropuertos)

val itsAir200C = itinerariosAire(vuelos200C, aeropuertos)
val itsAirPar200C = itinerariosAirePar(vuelos200C, aeropuertos)

val itsSal200C = itinerarioSalida(vuelos200C, aeropuertos)
val itsSalPar200C = itinerarioSalidaPar(vuelos200C, aeropuertos)

// ========== 1. PRUEBAS PARA itinerarios() ==========
println("\n" + "-" * 70)
println("1. itinerarios() vs itinerariosPar()")
println("-" * 70)

// Prueba 1.1
println("\nPrueba 1.1: ORD -> TPA")
val tiempo_its1_sec = tiempoDe(its200C("ORD", "TPA"))
val tiempo_its1_par = tiempoDe(itsPar200C("ORD", "TPA"))
val speedUp_its1 = speedUp(tiempo_its1_sec, tiempo_its1_par)
println(f"  Secuencial: ${toDouble(tiempo_its1_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_its1_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_its1}%6.2f x")

// Prueba 1.2
println("\nPrueba 1.2: DFW -> ORD")
val tiempo_its2_sec = tiempoDe(its200C("DFW", "ORD"))
val tiempo_its2_par = tiempoDe(itsPar200C("DFW", "ORD"))
val speedUp_its2 = speedUp(tiempo_its2_sec, tiempo_its2_par)
println(f"  Secuencial: ${toDouble(tiempo_its2_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_its2_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_its2}%6.2f x")

// Prueba 1.3
println("\nPrueba 1.3: HOU -> BNA")
val tiempo_its3_sec = tiempoDe(its200C("HOU", "BNA"))
val tiempo_its3_par = tiempoDe(itsPar200C("HOU", "BNA"))
val speedUp_its3 = speedUp(tiempo_its3_sec, tiempo_its3_par)
println(f"  Secuencial: ${toDouble(tiempo_its3_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_its3_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_its3}%6.2f x")

// Prueba 1.4
println("\nPrueba 1.4: SEA -> MIA")
val tiempo_its4_sec = tiempoDe(its200C("SEA", "MIA"))
val tiempo_its4_par = tiempoDe(itsPar200C("SEA", "MIA"))
val speedUp_its4 = speedUp(tiempo_its4_sec, tiempo_its4_par)
println(f"  Secuencial: ${toDouble(tiempo_its4_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_its4_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_its4}%6.2f x")

// Prueba 1.5
println("\nPrueba 1.5: ABQ -> TPA")
val tiempo_its5_sec = tiempoDe(its200C("ABQ", "TPA"))
val tiempo_its5_par = tiempoDe(itsPar200C("ABQ", "TPA"))
val speedUp_its5 = speedUp(tiempo_its5_sec, tiempo_its5_par)
println(f"  Secuencial: ${toDouble(tiempo_its5_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_its5_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_its5}%6.2f x")

// ========== 2. PRUEBAS PARA itinerariosTiempo() ==========
println("\n" + "-" * 70)
println("2. itinerariosTiempo() vs itinerariosTiempoPar()")
println("-" * 70)

// Prueba 2.1
println("\nPrueba 2.1: ORD -> TPA")
val tiempo_tpo1_sec = tiempoDe(itsTpo200C("ORD", "TPA"))
val tiempo_tpo1_par = tiempoDe(itsTpoPar200C("ORD", "TPA"))
val speedUp_tpo1 = speedUp(tiempo_tpo1_sec, tiempo_tpo1_par)
println(f"  Secuencial: ${toDouble(tiempo_tpo1_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_tpo1_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_tpo1}%6.2f x")

// Prueba 2.2
println("\nPrueba 2.2: DFW -> ORD")
val tiempo_tpo2_sec = tiempoDe(itsTpo200C("DFW", "ORD"))
val tiempo_tpo2_par = tiempoDe(itsTpoPar200C("DFW", "ORD"))
val speedUp_tpo2 = speedUp(tiempo_tpo2_sec, tiempo_tpo2_par)
println(f"  Secuencial: ${toDouble(tiempo_tpo2_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_tpo2_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_tpo2}%6.2f x")

// Prueba 2.3
println("\nPrueba 2.3: HOU -> BNA")
val tiempo_tpo3_sec = tiempoDe(itsTpo200C("HOU", "BNA"))
val tiempo_tpo3_par = tiempoDe(itsTpoPar200C("HOU", "BNA"))
val speedUp_tpo3 = speedUp(tiempo_tpo3_sec, tiempo_tpo3_par)
println(f"  Secuencial: ${toDouble(tiempo_tpo3_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_tpo3_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_tpo3}%6.2f x")

// Prueba 2.4
println("\nPrueba 2.4: SEA -> MIA")
val tiempo_tpo4_sec = tiempoDe(itsTpo200C("SEA", "MIA"))
val tiempo_tpo4_par = tiempoDe(itsTpoPar200C("SEA", "MIA"))
val speedUp_tpo4 = speedUp(tiempo_tpo4_sec, tiempo_tpo4_par)
println(f"  Secuencial: ${toDouble(tiempo_tpo4_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_tpo4_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_tpo4}%6.2f x")

// Prueba 2.5
println("\nPrueba 2.5: ABQ -> TPA")
val tiempo_tpo5_sec = tiempoDe(itsTpo200C("ABQ", "TPA"))
val tiempo_tpo5_par = tiempoDe(itsTpoPar200C("ABQ", "TPA"))
val speedUp_tpo5 = speedUp(tiempo_tpo5_sec, tiempo_tpo5_par)
println(f"  Secuencial: ${toDouble(tiempo_tpo5_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_tpo5_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_tpo5}%6.2f x")

// ========== 3. PRUEBAS PARA itinerariosEscalas() ==========
println("\n" + "-" * 70)
println("3. itinerariosEscalas() vs itinerariosEscalasPar()")
println("-" * 70)

// Prueba 3.1
println("\nPrueba 3.1: ORD -> TPA")
val tiempo_esc1_sec = tiempoDe(itsEsc200C("ORD", "TPA"))
val tiempo_esc1_par = tiempoDe(itsEscPar200C("ORD", "TPA"))
val speedUp_esc1 = speedUp(tiempo_esc1_sec, tiempo_esc1_par)
println(f"  Secuencial: ${toDouble(tiempo_esc1_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_esc1_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_esc1}%6.2f x")

// Prueba 3.2
println("\nPrueba 3.2: DFW -> ORD")
val tiempo_esc2_sec = tiempoDe(itsEsc200C("DFW", "ORD"))
val tiempo_esc2_par = tiempoDe(itsEscPar200C("DFW", "ORD"))
val speedUp_esc2 = speedUp(tiempo_esc2_sec, tiempo_esc2_par)
println(f"  Secuencial: ${toDouble(tiempo_esc2_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_esc2_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_esc2}%6.2f x")

// Prueba 3.3
println("\nPrueba 3.3: HOU -> BNA")
val tiempo_esc3_sec = tiempoDe(itsEsc200C("HOU", "BNA"))
val tiempo_esc3_par = tiempoDe(itsEscPar200C("HOU", "BNA"))
val speedUp_esc3 = speedUp(tiempo_esc3_sec, tiempo_esc3_par)
println(f"  Secuencial: ${toDouble(tiempo_esc3_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_esc3_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_esc3}%6.2f x")

// Prueba 3.4
println("\nPrueba 3.4: SEA -> MIA")
val tiempo_esc4_sec = tiempoDe(itsEsc200C("SEA", "MIA"))
val tiempo_esc4_par = tiempoDe(itsEscPar200C("SEA", "MIA"))
val speedUp_esc4 = speedUp(tiempo_esc4_sec, tiempo_esc4_par)
println(f"  Secuencial: ${toDouble(tiempo_esc4_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_esc4_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_esc4}%6.2f x")

// Prueba 3.5
println("\nPrueba 3.5: ABQ -> TPA")
val tiempo_esc5_sec = tiempoDe(itsEsc200C("ABQ", "TPA"))
val tiempo_esc5_par = tiempoDe(itsEscPar200C("ABQ", "TPA"))
val speedUp_esc5 = speedUp(tiempo_esc5_sec, tiempo_esc5_par)
println(f"  Secuencial: ${toDouble(tiempo_esc5_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_esc5_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_esc5}%6.2f x")

// ========== 4. PRUEBAS PARA itinerariosAire() ==========
println("\n" + "-" * 70)
println("4. itinerariosAire() vs itinerariosAirePar()")
println("-" * 70)

// Prueba 4.1
println("\nPrueba 4.1: ORD -> TPA")
val tiempo_air1_sec = tiempoDe(itsAir200C("ORD", "TPA"))
val tiempo_air1_par = tiempoDe(itsAirPar200C("ORD", "TPA"))
val speedUp_air1 = speedUp(tiempo_air1_sec, tiempo_air1_par)
println(f"  Secuencial: ${toDouble(tiempo_air1_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_air1_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_air1}%6.2f x")

// Prueba 4.2
println("\nPrueba 4.2: DFW -> ORD")
val tiempo_air2_sec = tiempoDe(itsAir200C("DFW", "ORD"))
val tiempo_air2_par = tiempoDe(itsAirPar200C("DFW", "ORD"))
val speedUp_air2 = speedUp(tiempo_air2_sec, tiempo_air2_par)
println(f"  Secuencial: ${toDouble(tiempo_air2_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_air2_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_air2}%6.2f x")

// Prueba 4.3
println("\nPrueba 4.3: HOU -> BNA")
val tiempo_air3_sec = tiempoDe(itsAir200C("HOU", "BNA"))
val tiempo_air3_par = tiempoDe(itsAirPar200C("HOU", "BNA"))
val speedUp_air3 = speedUp(tiempo_air3_sec, tiempo_air3_par)
println(f"  Secuencial: ${toDouble(tiempo_air3_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_air3_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_air3}%6.2f x")

// Prueba 4.4
println("\nPrueba 4.4: SEA -> MIA")
val tiempo_air4_sec = tiempoDe(itsAir200C("SEA", "MIA"))
val tiempo_air4_par = tiempoDe(itsAirPar200C("SEA", "MIA"))
val speedUp_air4 = speedUp(tiempo_air4_sec, tiempo_air4_par)
println(f"  Secuencial: ${toDouble(tiempo_air4_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_air4_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_air4}%6.2f x")

// Prueba 4.5
println("\nPrueba 4.5: ABQ -> TPA")
val tiempo_air5_sec = tiempoDe(itsAir200C("ABQ", "TPA"))
val tiempo_air5_par = tiempoDe(itsAirPar200C("ABQ", "TPA"))
val speedUp_air5 = speedUp(tiempo_air5_sec, tiempo_air5_par)
println(f"  Secuencial: ${toDouble(tiempo_air5_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_air5_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_air5}%6.2f x")

// ========== 5. PRUEBAS PARA itinerarioSalida() ==========
println("\n" + "-" * 70)
println("5. itinerarioSalida() vs itinerarioSalidaPar()")
println("-" * 70)

// Prueba 5.1
println("\nPrueba 5.1: ORD -> TPA a las 18:30")
val tiempo_sal1_sec = tiempoDe(itsSal200C("ORD", "TPA", 18, 30))
val tiempo_sal1_par = tiempoDe(itsSalPar200C("ORD", "TPA", 18, 30))
val speedUp_sal1 = speedUp(tiempo_sal1_sec, tiempo_sal1_par)
println(f"  Secuencial: ${toDouble(tiempo_sal1_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_sal1_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_sal1}%6.2f x")

// Prueba 5.2
println("\nPrueba 5.2: DFW -> ORD a las 18:30")
val tiempo_sal2_sec = tiempoDe(itsSal200C("DFW", "ORD", 18, 30))
val tiempo_sal2_par = tiempoDe(itsSalPar200C("DFW", "ORD", 18, 30))
val speedUp_sal2 = speedUp(tiempo_sal2_sec, tiempo_sal2_par)
println(f"  Secuencial: ${toDouble(tiempo_sal2_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_sal2_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_sal2}%6.2f x")

// Prueba 5.3
println("\nPrueba 5.3: HOU -> BNA a las 18:30")
val tiempo_sal3_sec = tiempoDe(itsSal200C("HOU", "BNA", 18, 30))
val tiempo_sal3_par = tiempoDe(itsSalPar200C("HOU", "BNA", 18, 30))
val speedUp_sal3 = speedUp(tiempo_sal3_sec, tiempo_sal3_par)
println(f"  Secuencial: ${toDouble(tiempo_sal3_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_sal3_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_sal3}%6.2f x")

// Prueba 5.4
println("\nPrueba 5.4: SEA -> MIA a las 18:30")
val tiempo_sal4_sec = tiempoDe(itsSal200C("SEA", "MIA", 18, 30))
val tiempo_sal4_par = tiempoDe(itsSalPar200C("SEA", "MIA", 18, 30))
val speedUp_sal4 = speedUp(tiempo_sal4_sec, tiempo_sal4_par)
println(f"  Secuencial: ${toDouble(tiempo_sal4_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_sal4_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_sal4}%6.2f x")

// Prueba 5.5
println("\nPrueba 5.5: ABQ -> TPA a las 18:30")
val tiempo_sal5_sec = tiempoDe(itsSal200C("ABQ", "TPA", 18, 30))
val tiempo_sal5_par = tiempoDe(itsSalPar200C("ABQ", "TPA", 18, 30))
val speedUp_sal5 = speedUp(tiempo_sal5_sec, tiempo_sal5_par)
println(f"  Secuencial: ${toDouble(tiempo_sal5_sec)}%8.2f ms")
println(f"  Paralelo:   ${toDouble(tiempo_sal5_par)}%8.2f ms")
println(f"  Speed-up:   ${speedUp_sal5}%6.2f x")

// ========== RESUMEN FINAL ==========
println("\n" + "=" * 70)
println("RESUMEN FINAL - PROMEDIOS POR FUNCIÓN")
println("=" * 70)

// Calcular promedios - versión corregida para trabajar con Quantity[Double]
def calcularPromedioSpeedUp(tiemposSec: List[org.scalameter.Quantity[Double]], tiemposPar: List[org.scalameter.Quantity[Double]]): Double = {
  val totalSec = tiemposSec.map(toDouble).sum
  val totalPar = tiemposPar.map(toDouble).sum
  if (totalPar > 0.0) totalSec / totalPar else 0.0
}

// Promedio itinerarios
val speedUp_its = calcularPromedioSpeedUp(
  List(tiempo_its1_sec, tiempo_its2_sec, tiempo_its3_sec, tiempo_its4_sec, tiempo_its5_sec),
  List(tiempo_its1_par, tiempo_its2_par, tiempo_its3_par, tiempo_its4_par, tiempo_its5_par)
)

// Promedio itinerariosTiempo
val speedUp_tpo = calcularPromedioSpeedUp(
  List(tiempo_tpo1_sec, tiempo_tpo2_sec, tiempo_tpo3_sec, tiempo_tpo4_sec, tiempo_tpo5_sec),
  List(tiempo_tpo1_par, tiempo_tpo2_par, tiempo_tpo3_par, tiempo_tpo4_par, tiempo_tpo5_par)
)

// Promedio itinerariosEscalas
val speedUp_esc = calcularPromedioSpeedUp(
  List(tiempo_esc1_sec, tiempo_esc2_sec, tiempo_esc3_sec, tiempo_esc4_sec, tiempo_esc5_sec),
  List(tiempo_esc1_par, tiempo_esc2_par, tiempo_esc3_par, tiempo_esc4_par, tiempo_esc5_par)
)

// Promedio itinerariosAire
val speedUp_air = calcularPromedioSpeedUp(
  List(tiempo_air1_sec, tiempo_air2_sec, tiempo_air3_sec, tiempo_air4_sec, tiempo_air5_sec),
  List(tiempo_air1_par, tiempo_air2_par, tiempo_air3_par, tiempo_air4_par, tiempo_air5_par)
)

// Promedio itinerarioSalida
val speedUp_sal = calcularPromedioSpeedUp(
  List(tiempo_sal1_sec, tiempo_sal2_sec, tiempo_sal3_sec, tiempo_sal4_sec, tiempo_sal5_sec),
  List(tiempo_sal1_par, tiempo_sal2_par, tiempo_sal3_par, tiempo_sal4_par, tiempo_sal5_par)
)

println("\nPROMEDIOS DE SPEED-UP (Aceleración):")
println(f"itinerarios():         ${speedUp_its}%6.2f x")
println(f"itinerariosTiempo():   ${speedUp_tpo}%6.2f x")
println(f"itinerariosEscalas():  ${speedUp_esc}%6.2f x")
println(f"itinerariosAire():     ${speedUp_air}%6.2f x")
println(f"itinerarioSalida():    ${speedUp_sal}%6.2f x")

// Speed-up promedio general
val speedUpGeneral = (speedUp_its + speedUp_tpo + speedUp_esc + speedUp_air + speedUp_sal) / 5.0
println(f"\nSPEED-UP PROMEDIO GENERAL: ${speedUpGeneral}%6.2f x")

println("\n" + "=" * 70)
println("FIN DE LAS PRUEBAS DE RENDIMIENTO")
println("=" * 70)