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

  /*
  //Funcion auxiliar para convertir el tiempo
  def calcularTiempoTotal(itinerario: Itinerario, aeropuertos: Map[String, Aeropuerto]): Int = {
    itinerario.map { vuelo =>
      val origen = aeropuertos(vuelo.Org)
      val destino = aeropuertos(vuelo.Dst)
      val salidaMinutos = vuelo.HS * 60 + vuelo.MS
      val llegadaMinutos = vuelo.HL * 60 + vuelo.ML
      val diferenciaGMT = destino.GMT - origen.GMT
      val tiempoVuelo = (llegadaMinutos + diferenciaGMT * 60) - salidaMinutos
      if (tiempoVuelo < 0) tiempoVuelo + 24 * 60 else tiempoVuelo
    }.sum
  }
   */



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

    (origen: String, destino: String) => {
      itinerarios(vuelos, aeropuertos)(origen, destino)
        .sortBy(itinerario =>
          itinerario.map(_.Esc).sum
            + itinerario.length - 1
        )
        .take(3)
    }
  }

  //3.4

  def itinerariosAire(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    def distancia(a1: Aeropuerto, a2: Aeropuerto): Double = {
      val dx = a1.X - a2.X
      val dy = a1.Y - a2.Y
      Math.sqrt(dx * dx + dy * dy)
    }

    def encontrarAeropuerto(cod: String): Option[Aeropuerto] = {
      aeropuertos.find(_.Cod == cod)
    }

    def dfs(actual: String, destino: String, ruta: List[Vuelo], distTotal: Double, visitados: Set[String], mejores: collection.mutable.PriorityQueue[(List[Vuelo], Double)]): List[Itinerario] = {
      if (actual == destino) {
        if (mejores.size < 3) {
          mejores.enqueue((ruta, distTotal))
        }

        else if (distTotal < mejores.head._2) {
          mejores.dequeue()
          mejores.enqueue((ruta, distTotal))
        }
        List()
      }

      else {
        val posiblesVuelos = vuelos.filter(v => v.Org == actual && !visitados.contains(v.Dst))
        posiblesVuelos.foldLeft(List[Itinerario]()) { (acc, vuelo) =>
          val dist = encontrarAeropuerto(vuelo.Org).flatMap(origen => encontrarAeropuerto(vuelo.Dst).map(destino => distancia(origen, destino)))
          dfs(vuelo.Dst, destino, ruta :+ vuelo, distTotal + dist.getOrElse(Double.MaxValue), visitados + actual, mejores) ::: acc
        }
      }
    }

    (cod1: String, cod2: String) => {
      val mejores = collection.mutable.PriorityQueue.empty(Ordering.by[(List[Vuelo], Double), Double](_._2).reverse)
      val visitados = Set[String]()
      dfs(cod1, cod2, List(), 0.0, visitados, mejores)
      mejores.map(_._1).toList
    }
  }
}