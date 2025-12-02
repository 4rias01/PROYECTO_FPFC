import common._
import Datos._
import scala.collection.parallel.CollectionConverters._ //revisar


package object ItinerariosPar {

  /**
   * Versión paralelizada de la función `itinerarios`, basada en paralelismo
   * de datos mediante colecciones paralelas. Esta función genera todos los
   * itinerarios posibles entre dos aeropuertos dados, pero permite que la
   * exploración de los vuelos salientes desde cada aeropuerto se realice en
   * paralelo, aprovechando múltiples núcleos disponibles.
   *
   * La idea principal viene al convertir la colección de vuelos a una
   * colección paralela (`vuelos.par`), las operaciones de filtrado y
   * mapeo que se realizan durante la búsqueda en profundidad (DFS)
   * pueden distribuirse automáticamente entre varios hilos gestionados
   * por la JVM, sin recurrir a estructuras de control imperativas.
   *
   * Semánticamente, la función sigue exactamente el comportamiento de la
   * versión secuencial: se realiza un DFS prohibiendo ciclos mediante
   * el conjunto `visitados`, y cada vez que el aeropuerto actual coincide
   * con el destino solicitado, se devuelve el itinerario construido.
   * Sin embargo, el for-comprehension que recorre los vuelos salientes
   * opera ahora sobre una colección paralela, permitiendo que las
   * ramificaciones de la búsqueda se evalúen concurrentemente.
   */
  def itinerariosPar(
                      vuelos: List[Vuelo],
                      aeropuertos: List[Aeropuerto]
                    ): (String, String) => List[Itinerario] = {

    // Colección paralela de vuelos para reutilizar en toda la recursión
    val vuelosPar = vuelos.par

    def buscarItinerariosPar(org: String,
                             dst: String,
                             visitados: Set[String],
                             itinerarioActual: Itinerario): List[Itinerario] = {

      if (org == dst) {
        // Caso base: ya llegamos al destino, devolvemos el itinerario construido
        List(itinerarioActual)
      } else {
        // Recorremos en paralelo todos los vuelos que salen de 'org'
        // y que no conducen a un aeropuerto ya visitado.
        val resultadosPar = for {
          vuelo <- vuelosPar
          if vuelo.Org == org && !visitados.contains(vuelo.Dst)
          newVisitados   = visitados + org
          newItinerario  = itinerarioActual :+ vuelo
          resultado <- buscarItinerariosPar(
            vuelo.Dst,
            dst,
            newVisitados,
            newItinerario
          )
        } yield resultado

        // Convertimos de colección paralela a List secuencial
        resultadosPar.toList
      }
    }

    (c1: String, c2: String) =>
      buscarItinerariosPar(c1, c2, Set.empty[String], List.empty[Vuelo])
  }

  /**
   * Versión paralelizada de `itinerariosTiempo`, que selecciona los tres
   * itinerarios con menor tiempo total de viaje entre dos aeropuertos, pero
   * incorporando dos niveles de paralelismo: paralelismo de datos y
   * paralelismo de tareas.
   *
   * 1. Paralelismo sobre itinerarios (paralelismo de datos):
   *    ---------------------------------------------------------------------
   *    La función `itinerariosPar` se utiliza para generar todos los
   *    itinerarios posibles entre `c1` y `c2`. Esta versión paralela del
   *    generador reparte automáticamente la exploración de alternativas de
   *    vuelos entre varios hilos mediante colecciones paralelas, evitando
   *    estructuras imperativas y sin necesidad de crear hilos manualmente.
   *
   *    Posteriormente, la lista resultante de itinerarios se procesa con
   *    `.par.map(...)`, permitiendo que el cálculo del tiempo total de cada
   *    itinerario se evalúe en paralelo. Cada itinerario es independiente,
   *    por lo que este paralelismo es seguro y naturalmente expresable como
   *    paralelismo de datos, tal como se describe en la Clase 12.
   *
   * 2. Paralelismo dentro de cada itinerario (paralelismo de tareas):
   *    ---------------------------------------------------------------------
   *    El tiempo total de un itinerario se compone de:
   *      - tiempo total en aire (suma de duraciones de todos los vuelos),
   *      - tiempo total en escalas (suma de tiempos de espera entre vuelos).
   *
   *    Estos dos cálculos son completamente independientes, por lo que se
   *    evalúan de manera simultánea mediante la construcción `parallel(e1, e2)`
   *    introducida en la Clase 11. De esta manera se explotan múltiples núcleos
   *    incluso dentro del análisis de un único itinerario, combinando así
   *    paralelismo de tareas con paralelismo de datos.
   *
   * 3. Selección final de los mejores itinerarios:
   *    ---------------------------------------------------------------------
   *    Una vez calculado el tiempo total de cada itinerario, la lista se ordena
   *    por dicho valor y se seleccionan los tres con menor duración. Toda la
   *    implementación permanece estrictamente funcional: no se utilizan
   *    estructuras mutables, no existe coordinación explícita entre hilos y el
   *    paralelismo lo gestiona internamente la librería estándar de Scala.
   *
   * En conjunto, esta función ilustra adecuadamente cómo combinar ambos modelos
   * de paralelismo estudiados en el curso: paralelismo de datos para procesar
   * múltiples elementos de manera independiente y paralelismo de tareas para
   * dividir el cálculo de una única unidad de trabajo en subcómputos que pueden
   * ejecutarse simultáneamente.
   */
  def itinerariosTiempoPar(
                            vuelos: List[Vuelo],
                            aeropuertos: List[Aeropuerto]
                          ): (String, String) => List[Itinerario] = {

    val aeropuertosMap: Map[String, Aeropuerto] =
      aeropuertos.map(a => a.Cod -> a).toMap

    // Versión con paralelismo de tareas dentro del cálculo
    def calcularTiempoTotal(itinerario: Itinerario, aeropuertos: Map[String, Aeropuerto]): Int = {

      def offsetMinutos(gmt: Int): Int = (gmt / 100) * 60

      def minutosUTC(hora: Int, minuto: Int, gmt: Int): Int = {
        val totalMinutos = hora * 60 + minuto
        totalMinutos - offsetMinutos(gmt)
      }

      def tiempoVuelo(vuelo: Vuelo): Int = {
        val origen  = aeropuertos(vuelo.Org)
        val destino = aeropuertos(vuelo.Dst)
        val salidaUTC  = minutosUTC(vuelo.HS, vuelo.MS, origen.GMT)
        val llegadaUTC = minutosUTC(vuelo.HL, vuelo.ML, destino.GMT)
        val tiempo = llegadaUTC - salidaUTC
        if (tiempo < 0) tiempo + 24 * 60 else tiempo
      }

      def tiempoEspera(vueloAnterior: Vuelo, vueloSiguiente: Vuelo): Int = {
        val destinoAnterior = aeropuertos(vueloAnterior.Dst)
        val origenSiguiente = aeropuertos(vueloSiguiente.Org)
        val llegadaUTC = minutosUTC(vueloAnterior.HL, vueloAnterior.ML, destinoAnterior.GMT)
        val salidaUTC  = minutosUTC(vueloSiguiente.HS, vueloSiguiente.MS, origenSiguiente.GMT)
        val espera = salidaUTC - llegadaUTC
        if (espera < 0) espera + 24 * 60 else espera
      }

      // Aquí viene la gracia:
      // calculamos tiempoEnAire y tiempoEnEscala en paralelo
      val (tiempoEnAire, tiempoEnEscala) = parallel(
        // tarea 1: sumar tiempos de vuelo
        itinerario.map(tiempoVuelo).sum,
        // tarea 2: sumar tiempos de espera entre vuelos consecutivos
        itinerario
          .sliding(2)
          .map {
            case List(v1, v2) => tiempoEspera(v1, v2)
            case _            => 0
          }
          .sum
      )

      tiempoEnAire + tiempoEnEscala
    }

    // Reutilizamos la versión paralela de itinerarios
    val obtenerItinerariosPar = itinerariosPar(vuelos, aeropuertos)

    (c1: String, c2: String) => {
      val todosItinerarios: List[Itinerario] =
        obtenerItinerariosPar(c1, c2)

      // Paralelismo de datos sobre itinerarios
      val itinerariosConTiempo: List[(Itinerario, Int)] =
        todosItinerarios
          .par
          .map(it => (it, calcularTiempoTotal(it, aeropuertosMap)))
          .toList

      val mejoresItinerarios =
        itinerariosConTiempo
          .sortBy(_._2)
          .take(3)

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
        else {
          val (a,b) = it.splitAt(it.length / 2)
          val (izq,der) = parallel(tiemposPar(a)(f),tiemposPar(b)(f))
          izq ++ der
        }
      }

      val ListaItinenarios = itinerariosPosibles(cod1, cod2)
      val tiempos = tiemposPar(ListaItinenarios)(tiempoEnAirePar)
      val pares = ListaItinenarios.zip(tiempos)
      pares.sortBy(_._2).take(3).map(_._1)
    }
  }
}