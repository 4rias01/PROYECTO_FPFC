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