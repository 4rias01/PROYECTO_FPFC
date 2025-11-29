import org.scalameter._
package object Datos {

  case class Aeropuerto(Cod: String, X: Int, Y: Int, GMT: Int)
  case class Vuelo(Aln: String, Num: Int, Org: String, HS: Int, MS: Int, Dst: String, HL: Int, ML: Int, Esc: Int)

  type Itinerario = List[Vuelo]

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

  val aeropuertosCurso = List(
    Aeropuerto("CLO", 100, 200, -500), // Cali
    Aeropuerto("BOG", 300, 500, -500), // Bogotá
    Aeropuerto("MDE", 200, 600, -500), // Medellín
    Aeropuerto("BAQ", 350, 850, -500), // Barranquilla
    Aeropuerto("SMR", 400, 950, -500), // Santa Marta
    Aeropuerto("CTG", 300, 800, -500), // Cartagena
    Aeropuerto("PTY", 400, 1000, -500), // Ciudad de Panamá
    Aeropuerto("JFK", 2000, 2000, -400), // Nueva York
    Aeropuerto("MIA", 1000, 2000, -500), // Miami
    Aeropuerto("MEX", 1000, 1000, -600), // Ciudad de México
    Aeropuerto("MAD", 5000, 5000, 100), // Madrid
    Aeropuerto("SVCS", 400, 1000, -600), // Caracas
    Aeropuerto("MID", 500, 100, -600), // Mérida
    Aeropuerto("AUA", 500, 2000, -400), // Aruba
    Aeropuerto("IST", 9000, 9000, 300), // Estambul
    Aeropuerto("HND", 10000, 12000, 900), // Tokio
    Aeropuerto("DXB", 9500, 11500, 400), // Dubái
    Aeropuerto("SVO", 12500, 12500, 300) // Moscú
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


  val vuelosPrueba = List(
    Vuelo("AIRVZLA", 601, "CLO", 5, 0, "BOG", 7, 0, 0),
    Vuelo("AIRVZLA", 602, "BOG", 8, 30, "BAQ", 13, 30, 0),
    Vuelo("AVA", 9432, "BAQ", 14, 0, "DXB", 2, 0, 0),
  )
  //type ItinerarioPar = ParSeq[Vuelo]
  // Venezuela aislada, Cali Moscu, Cali CDMX pasando por Bogota vs pasando por Mde - Baq, CTG - PTY directo o pasando
  // por SMR, Cali Moscu por Estambul tiene 4 escalas pero 2 vuelos, Cali Moscu por Madrid tiene 3 vuelos y 3 escalas
  // pero llega más temprano
}
