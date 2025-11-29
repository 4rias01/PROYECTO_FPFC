import Itinerarios2._
import Datos._
import org.scalameter.{Key, KeyValue, Warmer, config}
import scala.util.Random

val itsCurso = itinerarios(vuelosCurso, aeropuertosCurso)
val its1 = itsCurso("MID", "SVCS")
val its2 = itsCurso("CLO", "SVCS")

// 4 itinerarios CLO–SVO
val its3 = itsCurso("CLO", "SVO")

// 2 itinerarios CLO–MEX
val its4 = itsCurso("CLO", "MEX")

// 2 itinerarios CTG–PTY
val its5 = itsCurso("CTG", "PTY")


val itsTiempoCurso = itinerariosTiempo2(vuelosCurso, aeropuertosCurso)

val itst1 = itsTiempoCurso("MID", "SVCS")

val itst2 = itsTiempoCurso("CLO", "SVCS")

val itst3 = itsTiempoCurso("CLO", "SVO")

val itst4 = itsTiempoCurso("CLO", "MEX")

val itst5 = itsTiempoCurso("CTG", "PTY")

val itSalidaCurso = ItinerariosSalida(vuelosCurso, aeropuertosCurso)

val itsal1  = itSalidaCurso("CTG","PTY",11,40)

val itsal2 = itSalidaCurso("CTG","PTY",11,55)

val itsal3 = itSalidaCurso("CTG","PTY",11,30)

