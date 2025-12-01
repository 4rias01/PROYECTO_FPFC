import common._
import Datos._
import scala.collection.parallel.CollectionConverters._ //revisar
package object ItinerariosPar {

  // 3.1
  def itinerariosPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    def buscarItinerarios(Org: String, Dst: String, visitados: Set[String], itinerarioActual: Itinerario): List[Itinerario] = {
      if (Org == Dst) {
        return List(itinerarioActual)
      }

      val resultadosParalelos = vuelos.filter(v => v.Org == Org && !visitados.contains(v.Dst)).map { vuelo =>
        task {
          val newVisitados = visitados + Org
          val newItinerary = itinerarioActual :+ vuelo
          buscarItinerarios(vuelo.Dst, Dst, newVisitados, newItinerary)
        }
      }

      resultadosParalelos.flatMap(_.join())
    }

    (c1: String, c2: String) => buscarItinerarios(c1, c2, Set(), List())
  }

  // Funcion auxiliar para convertir el tiempo
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

  // 3.2
  def itinerariosTiempoPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {
    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap
    val obtenerItinerarios = itinerariosPar(vuelos, aeropuertos)

    (c1: String, c2: String) => {
      val todosItinerarios = obtenerItinerarios(c1, c2)
      val itinerariosConTiempo = todosItinerarios.map(it => task {
        (it, calcularTiempoTotal(it, aeropuertosMap))
      })

      val mejoresItinerarios = itinerariosConTiempo.map(_.join()).sortBy(_._2).take(3)
      mejoresItinerarios.map(_._1)
    }
  }

  //3.3
  def itinerariosEscalasPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {
    (origen: String, destino: String) => {
      val obtenerItinerarios = itinerariosPar(vuelos, aeropuertos)(origen, destino)

      def escalasTotales(itinerario: Itinerario): Int = {
        itinerario.map(_.Esc).sum + (itinerario.length - 1)
      }

      obtenerItinerarios
        .map(itinerario => task((itinerario, escalasTotales(itinerario))))
        .map(_.join())
        .sortBy(_._2)
        .take(3)
        .map(_._1)
    }
  }
  //3.4

  /**
   * Para la implementacion de ItinerariosAirePar se usó paralelizacion de tareas, con la abtracción parallel(a,b).
   *
   * Se paralelizó la funcion auxiliar "tiempoEnAire", si el número de vuelos del itinerario supera el umbral establecido
   * se divide la lista de vuelos en dos mitades y se calculan en paralelo sus tiempos en aire, sumandolos después, con 
   * el fin de paralelizar el cálculo de tiempo total de un itinerario.
   *
   * Luego, para el calulo total de sobre la colección de los itinerarios posibles, se creó la funcion auxiliar 
   * "tiemposPar", la cual para listas de itinerarios mayores que el umbral, se divide la lista en dos y se ejecutan 
   * en paralelo su tiempo total. Retorna la concatencacion del tiempo en aire total para cada itinerario
   *
   * por último se usa la funcion zip entre la lista de itinerarios posibles y la lista de tiempos para relacionar 
   * cada itinerario con su tiempo total correspondiente y se eligen los tres con menor tiempo
   */
  def itinerariosAirePar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap
    val itinerariosPosibles = itinerariosPar(vuelos, aeropuertos)

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

    def tiempoEnAirePar(itinerario: Itinerario): Double = {
      val umb = 5;
      if (itinerario.length <= umb)
        itinerario.map(tiempoVuelo).sum
      else {
        val (izq, der) = itinerario.splitAt(itinerario.length / 2)
        val (sum1, sum2) = parallel(tiempoEnAirePar(izq), tiempoEnAirePar(der))
        sum1 + sum2
      }
    }

    (cod1: String, cod2: String) => {

      def tiemposPar(it: List[Itinerario])(f:Itinerario => Double): List[Double] = {
        val umbral = 20
        if(it.length <= umbral)
          it.map(f)
        else
          val (a,b) = it.splitAt(it.length / 2)
          val (izq,der) = parallel(tiemposPar(a)(f),tiemposPar(b)(f))
          izq ++ der
      }

      val ListaItinenarios = itinerariosPosibles(cod1, cod2)
      val tiempos = tiemposPar(ListaItinenarios)(tiempoEnAirePar)
      val pares = ListaItinenarios.zip(tiempos)
      pares.sortBy(_._2).take(3).map(_._1)
    }
  }

}