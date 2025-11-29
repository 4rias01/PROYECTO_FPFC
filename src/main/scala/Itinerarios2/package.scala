import Datos._
package object Itinerarios2 {

  
  /**
  def itinerarios(vuelos:List[Vuelo],aeropuertos: List[Aeropuerto]):(String,String) => List[Itinerario] = {

    def buscarItinerarios(Org: String, Dst: String, visitados: Set[String]): List[Itinerario] = {
      if(Org == Dst)  List(Nil)
      else for {
        vuelo <- vuelos if vuelo.Org == Org && !visitados(vuelo.Dst)
        resto <- buscarItinerarios(vuelo.Dst, Dst, visitados + Org )
      } yield vuelo :: resto
    }

    (c1: String, c2: String) => buscarItinerarios(c1,c2,Set())
  }
  **/

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



  def calcularTiempoVuelo(vuelo: Vuelo,aeropuertos: Map[String, Aeropuerto], tipo: String): Int = {
    val origen = aeropuertos(vuelo.Org)  //Halla el aeropuerto de origen
    val destino = aeropuertos(vuelo.Dst) //halla el aeropuerto de destino
    val salidaMinutos = vuelo.HS * 60 + vuelo.MS //convierte la hora de salida en minutos
    val llegadaMinutos = vuelo.HL * 60 + vuelo.ML //convierte la hora de llegada en minutos
    val diferenciaGMT = destino.GMT - origen.GMT //Diferencia entre las zonas horarias (destino - origen)
    val llegadaEnHorarioOrigen = llegadaMinutos + diferenciaGMT * 60
    val duracion = if (llegadaEnHorarioOrigen < salidaMinutos) llegadaEnHorarioOrigen - salidaMinutos + 24 * 60 else llegadaEnHorarioOrigen  - salidaMinutos //calculo de la duracion del vuelo

    tipo.toLowerCase match {
      case "salida" => salidaMinutos //hora de salida (con la zona horaria del aeropuerto origen)
      case "llegada" => llegadaMinutos //hora de llegada (con la zona horaria del aeropuerto de destino)
      case "duracion" => duracion //duracion total del vuelo
    }

  }

  //3.2 Minimizacion de tiempo total de viaje
  def itinerariosTiempo2(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {
    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap
    val obtenerItinerarios = itinerarios(vuelos, aeropuertos)

    (c1: String, c2: String) => {
      val todosItinerarios = obtenerItinerarios(c1, c2)
      val itinerariosConTiempo = todosItinerarios.map {it =>
        val tiempoTotal = it.map(vuelo => calcularTiempoVuelo(vuelo, aeropuertosMap, "duracion")).sum
        (it,tiempoTotal) }
      val mejoresItinerarios = itinerariosConTiempo.sortBy(_._2).take(3)
      mejoresItinerarios.map(_._1)
    }
  }


  //3.5 Optimizacion de la hora de salida
  def ItinerariosSalida(vuelos:List[Vuelo], aeropuertos:List[Aeropuerto]): (String, String, Int, Int) => Itinerario = {
    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap
    val obtenerItinerarios = itinerarios(vuelos, aeropuertos)

    (c1: String, c2: String, h: Int, m: Int) => {
      val citaMinutos = h * 60 + m
      val todosItinerarios = obtenerItinerarios(c1,c2)

      //filtra los vuelos que llegan a tiempo ( antes de la hora local del destino)
      val validos = todosItinerarios.filter { it =>
        val ultimoVuelo = it.last
        val llegadaLocalDestino = calcularTiempoVuelo(ultimoVuelo,aeropuertosMap,"llegada")
        llegadaLocalDestino <= citaMinutos }
      if (validos.isEmpty) Nil
      // elige el vuelo que sale lo mas tarde del aeropuerto origen (hora local origen)
      else validos.maxBy { it =>
        val primerVuelo = it.head
        calcularTiempoVuelo(it.head,aeropuertosMap,"salida")
      }
    }
  }





}



