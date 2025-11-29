import Itinerarios._
import ItinerariosPar._
import Datos._
import org.scalameter.{Key, KeyValue, Warmer, config}

import scala.util.Random

//Funcion para comparar tiempos
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

// Datos de ejemplo
val aeropuertosCurso = List(
  Aeropuerto("CLO", 100, 200, -500),
  Aeropuerto("BOG", 300, 500, -500),
  Aeropuerto("MDE", 200, 600, -500),
  Aeropuerto("BAQ", 350, 850, -500),
  Aeropuerto("SMR", 400, 950, -500),
  Aeropuerto("CTG", 300, 800, -500),
  Aeropuerto("PTY", 400, 1000, -500),
  Aeropuerto("JFK", 2000, 2000, -400),
  Aeropuerto("MIA", 1000, 2000, -500),
  Aeropuerto("MEX", 1000, 1000, -600),
  Aeropuerto("MAD", 5000, 5000, 100),
  Aeropuerto("SVCS", 400, 1000, -600),
  Aeropuerto("MID", 500, 100, -600),
  Aeropuerto("AUA", 500, 2000, -400),
  Aeropuerto("IST", 9000, 9000, 300),
  Aeropuerto("HND", 10000, 12000, 900),
  Aeropuerto("DXB", 9500, 11500, 400),
  Aeropuerto("SVO", 12500, 12500, 300)
)

val vuelosCurso = List(
  Vuelo("AIRVZLA", 601, "MID", 5, 0, "SVCS", 6, 0, 0),
  Vuelo("AIRVZLA", 602, "SVCS", 6, 30, "MID", 7, 30, 0),
  Vuelo("AVA", 9432, "CLO", 7, 0, "SVO", 2, 20, 4),
  Vuelo("AVA", 9432, "CLO", 7, 0, "BOG", 8, 0, 0),
  Vuelo("IBERIA", 505, "BOG", 18, 0, "MAD", 12, 0, 0),
  Vuelo("IBERIA", 506, "MAD", 14, 0, "SVO", 23, 20, 0),
  Vuelo("IBERIA", 507, "MAD", 16, 0, "SVO", 1, 20, 0),
  Vuelo("LATAM", 787, "BOG", 17, 0, "MEX", 19, 0, 0),
  Vuelo("VIVA", 756, "BOG", 9, 0, "MDE", 10, 0, 0),
  Vuelo("VIVA", 769, "MDE", 11, 0, "BAQ", 12, 0, 0),
  Vuelo("AVA", 5643, "BAQ", 14, 0, "MEX", 16, 0, 0),
  Vuelo("COPA", 1234, "CTG", 10, 0, "PTY", 11, 30, 0),
  Vuelo("AVA", 4321, "CTG", 9, 30, "SMR", 10, 0, 0),
  Vuelo("COPA", 7631, "SMR", 10, 50, "PTY", 11, 50, 0),
  Vuelo("TURKISH", 7799, "CLO", 7, 0, "IST", 14, 0, 3),
  Vuelo("QATAR", 5566, "IST", 23, 0, "SVO", 2, 0, 0)
)


val aeropuertos=List(
  Aeropuerto("ABQ", 195, 275, -800),
  Aeropuerto("ATL", 470, 280, -600),
  Aeropuerto("BNA", 430, 240, -700),
  Aeropuerto("BOS", 590, 100, -600),
  Aeropuerto("DCA", 540, 180, -600),
  Aeropuerto("DEN", 215, 205, -800),
  Aeropuerto("DFW", 310, 305, -700),
  Aeropuerto("DTW", 445, 140, -600),
  Aeropuerto("HOU", 330, 355, -700),
  Aeropuerto("JFK", 565, 130, -600),
  Aeropuerto("LAX", 55, 270, -900),
  Aeropuerto("MIA", 535, 390, -600),
  Aeropuerto("MSP", 340, 115, -700),
  Aeropuerto("MSY", 405, 345, -700),
  Aeropuerto("ORD", 410, 155, -700),
  Aeropuerto("PHL", 550, 155, -600),
  Aeropuerto("PHX", 120, 290, -800),
  Aeropuerto("PVD", 595, 122, -600),
  Aeropuerto("RDU", 530, 230, -600),
  Aeropuerto("SEA", 55, 45, -900),
  Aeropuerto("SFO", 10, 190, -900),
  Aeropuerto("STL", 380, 210, -700),
  Aeropuerto("TPA", 500, 360, -600)
)

val vuelosC1= List(
  Vuelo("DL", 579, "ORD", 6, 20, "ATL", 10, 3, 0),
  Vuelo("DL", 842, "ATL", 19, 1, "PHL", 10, 58, 0),
  Vuelo("DL", 700, "ATL", 19, 2, "MSP", 10, 40, 0),
  Vuelo("DL", 955, "ATL", 23, 45, "DFW", 12, 45, 0),
  Vuelo("DL", 544, "DEN", 9, 40, "DFW", 12, 24, 0),
  Vuelo("DL", 509, "DFW", 18, 56, "DEN", 19, 55, 0),
  Vuelo("DL", 784, "DFW", 10, 20, "BNA", 12, 0, 0),
  Vuelo("DL", 247, "BOS", 17, 10, "MIA", 10, 22, 0),
  Vuelo("DL", 177, "ORD", 18, 50, "ATL", 11, 39, 0),
  Vuelo("DL", 679, "PVD", 17, 5, "ATL", 19, 36, 0),
  Vuelo("DL", 152, "SFO", 5, 30, "ATL", 14, 25, 1),
  Vuelo("DL", 675, "ATL", 19, 9, "DEN", 10, 30, 0),
  Vuelo("DL", 149, "TPA", 9, 20, "DFW", 10, 52, 0),
  Vuelo("DL", 350, "ATL", 9, 2, "BOS", 11, 25, 0),
  Vuelo("DL", 946, "DFW", 15, 1, "HOU", 16, 3, 0),
  Vuelo("DL", 775, "ATL", 8, 38, "DFW", 11, 8, 1),
  Vuelo("DL", 197, "TPA", 16, 10, "SEA", 11, 5, 1),
  Vuelo("DL", 818, "DTW", 19, 50, "ATL", 11, 37, 0),
  Vuelo("DL", 296, "PHX", 14, 25, "ATL", 19, 53, 0),
  Vuelo("DL", 178, "ATL", 18, 58, "DCA", 10, 35, 0),
  Vuelo("DL", 252, "PHX", 16, 35, "DFW", 19, 48, 0),
  Vuelo("DL", 148, "MIA", 18, 0, "ATL", 19, 54, 0),
  Vuelo("DL", 324, "TPA", 17, 45, "MSP", 11, 32, 1),
  Vuelo("DL", 257, "ATL", 11, 59, "DEN", 13, 5, 0),
  Vuelo("DL", 588, "PHX", 12, 55, "DFW", 16, 15, 0),
  Vuelo("DL", 874, "DFW", 8, 20, "DCA", 12, 5, 0),
  Vuelo("DL", 908, "DFW", 18, 41, "ATL", 13, 1, 1),
  Vuelo("DL", 990, "TPA", 12, 55, "ATL", 14, 22, 0),
  Vuelo("DL", 599, "BOS", 20, 30, "ATL", 13, 5, 0),
  Vuelo("DL", 480, "MSY", 17, 35, "ATL", 19, 55, 0),
  Vuelo("DL", 593, "ATL", 19, 3, "DFW", 11, 17, 1),
  Vuelo("DL", 863, "BOS", 12, 15, "TPA", 15, 18, 0),
  Vuelo("DL", 415, "DCA", 17, 45, "DFW", 19, 53, 0),
  Vuelo("DL", 979, "SFO", 19, 0, "LAX", 10, 12, 0),
  Vuelo("DL", 310, "DFW", 8, 20, "ORD", 10, 40, 0),
  Vuelo("DL", 389, "PHL", 19, 35, "TPA", 13, 45, 1),
  Vuelo("DL", 963, "SFO", 21, 0, "LAX", 12, 6, 0),
  Vuelo("DL", 252, "LAX", 13, 35, "DFW", 19, 48, 1),
  Vuelo("DL", 691, "DFW", 19, 4, "SFO", 10, 55, 0),
  Vuelo("DL", 412, "TPA", 14, 20, "ATL", 15, 42, 0),
  Vuelo("DL", 201, "DTW", 5, 50, "ATL", 13, 7, 0),
  Vuelo("DL", 179, "DFW", 15, 40, "LAX", 16, 40, 0),
  Vuelo("DL", 187, "ATL", 19, 7, "LAX", 10, 55, 0),
  Vuelo("DL", 629, "HOU", 8, 0, "DFW", 10, 3, 0),
  Vuelo("DL", 690, "HOU", 16, 50, "ORD", 11, 14, 1),
  Vuelo("DL", 134, "SFO", 15, 30, "ATL", 12, 47, 0),
  Vuelo("DL", 143, "LAX", 12, 50, "SFO", 15, 9, 0),
  Vuelo("DL", 887, "DFW", 15, 14, "SEA", 17, 10, 0),
  Vuelo("DL", 280, "ATL", 16, 49, "DCA", 18, 30, 0),
  Vuelo("DL", 275, "SFO", 14, 0, "LAX", 15, 12, 0),
  Vuelo("DL", 367, "BOS", 14, 20, "PHL", 15, 37, 0),
  Vuelo("DL", 868, "ATL", 8, 26, "DTW", 10, 10, 0),
  Vuelo("DL", 441, "DFW", 18, 42, "ABQ", 19, 40, 0),
  Vuelo("DL", 842, "HOU", 15, 5, "PHL", 10, 58, 1),
  Vuelo("DL", 359, "RDU", 20, 25, "ATL", 11, 39, 0),
  Vuelo("DL", 647, "DTW", 16, 50, "PHX", 10, 35, 1),
  Vuelo("DL", 696, "ATL", 10, 16, "STL", 10, 50, 0),
  Vuelo("DL", 577, "ATL", 20, 56, "MSY", 11, 30, 0),
  Vuelo("DL", 364, "DFW", 17, 24, "TPA", 10, 40, 0),
  Vuelo("DL", 130, "LAX", 15, 15, "ATL", 12, 9, 0),
  Vuelo("DL", 701, "DCA", 17, 59, "ATL", 19, 48, 0),
  Vuelo("DL", 714, "ATL", 23, 47, "DTW", 12, 5, 0),
  Vuelo("DL", 885, "BNA", 5, 35, "ATL", 13, 2, 0),
  Vuelo("DL", 704, "ORD", 16, 50, "ATL", 19, 44, 0),
  Vuelo("DL", 378, "ATL", 15, 19, "DCA", 16, 55, 0),
  Vuelo("DL", 527, "DFW", 11, 47, "ABQ", 12, 30, 0),
  Vuelo("DL", 545, "DFW", 15, 21, "ABQ", 16, 5, 0),
  Vuelo("DL", 139, "ATL", 15, 14, "LAX", 11, 20, 3),
  Vuelo("DL", 951, "DTW", 17, 45, "ATL", 19, 33, 0),
  Vuelo("DL", 720, "LAX", 13, 45, "DFW", 19, 34, 1),
  Vuelo("DL", 198, "SEA", 13, 45, "DFW", 19, 27, 0),
  Vuelo("DL", 701, "BOS", 15, 45, "ATL", 19, 48, 1),
  Vuelo("DL", 487, "BOS", 18, 50, "DFW", 11, 38, 0),
  Vuelo("DL", 588, "DFW", 17, 1, "DTW", 10, 30, 0),
  Vuelo("DL", 252, "PHX", 16, 35, "PHL", 12, 45, 1),
  Vuelo("DL", 662, "MSY", 10, 5, "ATL", 12, 21, 0),
  Vuelo("DL", 317, "DEN", 11, 0, "TPA", 18, 20, 1),
  Vuelo("DL", 323, "ATL", 11, 44, "DFW", 14, 30, 2),
  Vuelo("DL", 195, "ATL", 16, 49, "DFW", 18, 5, 0),
  Vuelo("DL", 367, "PHL", 16, 10, "MIA", 11, 20, 1),
  Vuelo("DL", 901, "ATL", 18, 57, "PHX", 11, 0, 0),
  Vuelo("DL", 110, "SFO", 8, 0, "JFK", 16, 13, 0),
  Vuelo("DL", 638, "LAX", 18, 0, "SFO", 19, 15, 0),
  Vuelo("DL", 161, "DFW", 19, 10, "LAX", 10, 20, 0),
  Vuelo("DL", 139, "DFW", 18, 52, "LAX", 11, 20, 1),
  Vuelo("DL", 492, "MIA", 16, 30, "BOS", 19, 28, 0),
  Vuelo("DL", 173, "LAX", 16, 55, "SEA", 19, 23, 0),
  Vuelo("DL", 35, "JFK", 17, 35, "LAX", 10, 43, 0),
  Vuelo("DL", 832, "DFW", 18, 45, "ATL", 11, 42, 0),
  Vuelo("DL", 34, "LAX", 8, 5, "JFK", 15, 59, 0),
  Vuelo("DL", 111, "JFK", 17, 40, "SFO", 10, 52, 0),
  Vuelo("DL", 178, "LAX", 10, 40, "DCA", 10, 35, 1),
  Vuelo("DL", 307, "ATL", 11, 44, "DFW", 14, 27, 1),
  Vuelo("DL", 868, "TPA", 6, 10, "DTW", 10, 10, 1),
  Vuelo("DL", 805, "SFO", 20, 0, "LAX", 11, 15, 0),
  Vuelo("DL", 141, "TPA", 17, 55, "LAX", 19, 53, 0),
  Vuelo("DL", 174, "SEA", 6, 30, "MIA", 16, 45, 1),
  Vuelo("DL", 784, "BNA", 12, 30, "ATL", 14, 28, 0),
  Vuelo("DL", 830, "SEA", 15, 25, "ATL", 12, 48, 0),
  Vuelo("DL", 722, "DFW", 16, 50, "ATL", 19, 44, 0)
)