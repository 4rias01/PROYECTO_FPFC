import Itinerarios._
import Datos._
import org.scalameter.{Key, KeyValue, Warmer, config}
import scala.util.Random

def compararAlgoritmos(a1: (String, String) => List[Itinerario], a2: (String, String) => List[Itinerario])
                      (origen: String, destino: String): (Double, Double, Double) = {

  val timeA1 = config(
    KeyValue(Key.exec.minWarmupRuns -> 20),
    KeyValue(Key.exec.maxWarmupRuns -> 60),
    KeyValue(Key.verbose -> false)
  ) withWarmer(new Warmer.Default) measure {
    a1(origen, destino)
  }

  val timeA2 = config(
    KeyValue(Key.exec.minWarmupRuns -> 20),
    KeyValue(Key.exec.maxWarmupRuns -> 60),
    KeyValue(Key.verbose -> false)
  ) withWarmer(new Warmer.Default) measure {
    a2(origen, destino)
  }

  val speedUp = timeA1.value / timeA2.value
  (timeA1.value, timeA2.value, speedUp)
}

val itsCurso = itinerarios(vuelosCurso, aeropuertosCurso)
val its1 = itsCurso("MID", "SVCS")
val its2 = itsCurso("CLO", "SVCS")

// 4 itinerarios CLO–SVO
val its3 = itsCurso("CLO", "SVO")

// 2 itinerarios CLO–MEX
val its4 = itsCurso("CLO", "MEX")

// 2 itinerarios CTG–PTY
val its5 = itsCurso("CTG", "PTY")

val itsTiempoCurso = itinerariosTiempo(vuelosCurso, aeropuertosCurso)

// Prueba itinerariosTiempo
val itst1 = itsTiempoCurso("MID", "SVCS")
val itst2 = itsTiempoCurso("CLO", "SVCS")

// 4 itinerarios CLO–SVO
val itst3 = itsTiempoCurso("CLO", "SVO")

// 2 itinerarios CLO–MEX
val itst4 = itsTiempoCurso("CLO", "MEX")

// 2 itinerarios CTG–PTY
val itst5 = itsTiempoCurso("CTG", "PTY")

// Obtener todas las escalas del curso
val itsEscalasCurso = itinerariosEscalas(vuelosCurso, aeropuertosCurso)

val itsc1 = itsEscalasCurso("MID", "SVCS")
val itsc2 = itsEscalasCurso("CLO", "SVCS")

// 4 itinerarios CLO–SVO
val itsc3 = itsEscalasCurso("CLO", "SVO")

// 2 itinerarios CLO–MEX
val itsc4 = itsEscalasCurso("CLO", "MEX")

// 2 itinerarios CTG–PTY
val itsc5 = itsEscalasCurso("CTG", "PTY")
