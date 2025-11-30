import Datos._
package object Itinerarios {

  type Itinerario = List[Vuelo]

  //3.1
  def itinerarios(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    def buscarItinerarios(Org: String, Dst: String, visitados: Set[String], itinerarioActual: Itinerario): List[Itinerario] = {
      if (Org == Dst) {
        List(itinerarioActual)
      }
      else {
        for {
          vuelo <- vuelos
          if vuelo.Org == Org && !visitados.contains(vuelo.Dst)
          newVisitados = visitados + Org
          newItinerary = itinerarioActual :+ vuelo
          resultado <- buscarItinerarios(vuelo.Dst, Dst, newVisitados, newItinerary)
        } yield resultado
      }
    }

    (c1: String, c2: String) => buscarItinerarios(c1, c2, Set(), List())
  }

  //3.2
  def itinerariosTiempo(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    def calcularTiempoTotal(itinerario: Itinerario, aeropuertos: Map[String, Aeropuerto]): Int = {

      def offsetMinutos(gmt: Int): Int = (gmt / 100) * 60
      def minutosUTC(hora: Int, minuto: Int, gmt: Int): Int = {
        val totalMinutos = hora * 60 + minuto
        totalMinutos - offsetMinutos(gmt)
      }

      def tiempoVuelo(vuelo: Vuelo): Int = {
        val origen = aeropuertos(vuelo.Org)
        val destino = aeropuertos(vuelo.Dst)
        val salidaUTC = minutosUTC(vuelo.HS, vuelo.MS, origen.GMT)
        val llegadaUTC = minutosUTC(vuelo.HL, vuelo.ML, destino.GMT)
        val tiempo = llegadaUTC - salidaUTC
        if (tiempo < 0) tiempo + 24 * 60 else tiempo
      }

      def tiempoEspera(vueloAnterior: Vuelo, vueloSiguiente: Vuelo): Int = {
        val destinoAnterior = aeropuertos(vueloAnterior.Dst)
        val origenSiguiente = aeropuertos(vueloSiguiente.Org)
        val llegadaUTC = minutosUTC(vueloAnterior.HL, vueloAnterior.ML, destinoAnterior.GMT)
        val salidaUTC = minutosUTC(vueloSiguiente.HS, vueloSiguiente.MS, origenSiguiente.GMT)
        val espera = salidaUTC - llegadaUTC
        if (espera < 0) espera + 24 * 60 else espera
      }

      val tiempoEnAire = itinerario.map(tiempoVuelo).sum
      val tiempoEnEscala = itinerario.sliding(2).map {
        case List(vueloAnterior, vueloSiguiente) => tiempoEspera(vueloAnterior, vueloSiguiente)
        case _ => 0
      }.sum

      tiempoEnAire + tiempoEnEscala
    }

    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap
    val obtenerItinerarios = itinerarios(vuelos, aeropuertos)

    (c1: String, c2: String) => {
      val todosItinerarios = obtenerItinerarios(c1, c2)
      val itinerariosConTiempo = todosItinerarios.map(it => (it, calcularTiempoTotal(it, aeropuertosMap)))
      val mejoresItinerarios = itinerariosConTiempo.sortBy(_._2).take(3)
      mejoresItinerarios.map(_._1)
    }
  }

  //3.3

  def itinerariosEscalas(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    def numeroEscalas(it: Itinerario): Int =
      it.map(_.Esc).sum + it.length - 1

    val itinerariosPosibles = itinerarios(vuelos, aeropuertos)
    (origen: String, destino: String) => {
      itinerariosPosibles(origen, destino)
        .sortBy(numeroEscalas)
        .take(3)
    }
  }

  //3.4

  def itinerariosAire(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap
    val itinerariosPosibles = itinerarios(vuelos, aeropuertos)

    def offsetMinutos(gmt: Int): Int = (gmt / 100) * 60
    def minutosUTC(hora: Int, minuto: Int, gmt: Int): Int = {
      val totalMinutos = hora * 60 + minuto
      totalMinutos - offsetMinutos(gmt)
    }
    
    def tiempoVuelo(vuelo: Vuelo): Double = {
      val origen = aeropuertosMap(vuelo.Org)
      val destino = aeropuertosMap(vuelo.Dst)
      val salidaUTC = minutosUTC(vuelo.HS, vuelo.MS, origen.GMT)
      val llegadaUTC = minutosUTC(vuelo.HL, vuelo.ML, destino.GMT)
      val tiempo = llegadaUTC - salidaUTC
      if (tiempo < 0) tiempo + 24 * 60 else tiempo
    }
    
    def tiempoEnAire(itinerario: Itinerario): Double = {
      itinerario.map(tiempoVuelo).sum
    }

    (cod1: String, cod2: String) => {
      itinerariosPosibles(cod1, cod2).
        sortBy(tiempoEnAire).
        take(3)
    }
  }
}