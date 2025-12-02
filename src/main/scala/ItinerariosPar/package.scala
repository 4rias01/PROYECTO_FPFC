import common.*
import Datos.*

import scala.annotation.tailrec
import scala.collection.parallel.CollectionConverters.*
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

    val vuelosPar = vuelos.par

    def buscarItinerariosPar(org: String,
                              dst: String,
                              visitados: Set[String],
                              itinerarioActual: Itinerario): List[Itinerario] = {

      if (org == dst) {
        List(itinerarioActual)
      } else {
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

      val (tiempoEnAire, tiempoEnEscala) = parallel(
        itinerario.map(tiempoVuelo).sum,
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


    val obtenerItinerariosPar = itinerariosPar(vuelos, aeropuertos)

    (c1: String, c2: String) => {
      val todosItinerarios: List[Itinerario] =
        obtenerItinerariosPar(c1, c2)

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


  /**
   * Versión paralelizada de `itinerariosEscalas`, que selecciona los tres itinerarios
   * entre dos aeropuertos con el menor número de escalas, calculado como la suma de las
   * escalas técnicas de los vuelos y los cambios de avión en el itinerario.
   *
   * La paralelización se realiza de la siguiente manera:
   *
   * 1. Paralelismo sobre itinerarios (paralelismo de datos):
   * ---------------------------------------------------------------------
   * La función `itinerariosPar` se utiliza para generar todos los itinerarios
   * posibles entre los aeropuertos `origen` y `destino`. La lista resultante de
   * itinerarios se convierte en una colección paralela con `.par`, lo que permite
   * que los cálculos de escalas se realicen de forma concurrente.
   *
   * 2. Paralelismo dentro del cálculo de escalas (paralelismo de tareas):
   * ---------------------------------------------------------------------
   * El cálculo del número de escalas para cada itinerario se distribuye entre varios
   * hilos de forma paralela. La métrica de número de escalas se calcula sumando las
   * escalas técnicas de cada vuelo y restando 1 por cada conexión de avión adicional.
   *
   * 3. Selección de los mejores itinerarios:
   * ---------------------------------------------------------------------
   * Después de calcular el número de escalas de cada itinerario, la lista de itinerarios
   * se ordena de menor a mayor número de escalas. Se seleccionan los tres itinerarios
   * con menor número de escalas, garantizando la eficiencia en el proceso.
   *
   * Esta función mantiene la estructura funcional, evitando el uso de estructuras mutables
   * o efectos secundarios, y utiliza el paralelismo de datos y tareas para mejorar el rendimiento
   * al calcular el número de escalas y al recorrer los itinerarios.
   */
  def itinerariosEscalasPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    val obtenerItinerarios = itinerariosPar(vuelos, aeropuertos)

    def numeroEscalas(it: Itinerario): Int =
      it.map(_.Esc).sum + it.length - 1

    (origen: String, destino: String) => {
      val todosItinerarios = obtenerItinerarios(origen, destino)

      val itinerariosConEscalas =
        todosItinerarios.par
          .map(it => (it, numeroEscalas(it)))
          .toList

      itinerariosConEscalas
        .sortBy(_._2)
        .take(3)
        .map(_._1)
    }
  }

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

  /**
   * Versión paralela de `itinerarioSalida`, cuya tarea consiste en seleccionar,
   * entre todos los itinerarios posibles entre dos aeropuertos, aquel que permita
   * llegar a una cita (dada por hora y minuto locales del aeropuerto destino)
   * saliendo lo más tarde posible.
   *
   * La función combina **paralelismo de datos** y **paralelismo de tareas**
   * siguiendo los modelos presentados en las clases del curso:
   *
   * 1. Paralelismo de datos en la generación de rutas
   * -------------------------------------------------------------------------
   * La función `itinerariosPar` se utiliza como generador base. Esta versión
   * paraleliza la exploración del grafo de vuelos mediante colecciones
   * paralelas, distribuyendo la búsqueda de itinerarios entre múltiples hilos.
   * De este modo, el conjunto completo de itinerarios posibles entre `c1` y `c2`
   * se obtiene en paralelo.
   *
   * 2. Paralelismo de datos al evaluar cada itinerario
   * -------------------------------------------------------------------------
   * Una vez generados los itinerarios, la operación `.par.map` se emplea
   * para calcular simultáneamente la diferencia con la hora de la cita
   * (`diferenciaItinerario`). Cada itinerario se procesa de manera
   * independiente, por lo que constituye un caso natural de paralelismo de
   * datos: una lista de trabajos homogéneos sin dependencias entre ellos.
   *
   * 3. Paralelismo de tareas dentro del cálculo del tiempo total
   * -------------------------------------------------------------------------
   * Para un itinerario fijo, el tiempo total se compone de:
   *      - tiempo en aire (suma de duraciones de todos los vuelos),
   *      - tiempo en escalas (suma de esperas entre vuelos consecutivos).
   *
   * Estos dos subcómputos son independientes, y por ello se evalúan en
   * paralelo mediante la construcción `parallel(e1, e2)`. Esto constituye un
   * ejemplo directo de paralelismo de tareas: dividir un problema único en
   * subproblemas independientes ejecutables simultáneamente.
   *
   * 4. Selección del mejor itinerario
   * -------------------------------------------------------------------------
   * Una vez paralelizados tanto la generación de rutas como su evaluación
   * temporal, se escoge el itinerario cuya diferencia respecto a la cita
   * sea mínima. La implementación se mantiene completamente funcional,
   * evitando efectos laterales, mutabilidad o estructuras imperativas.
   *
   * En conjunto, esta función demuestra la correcta integración de ambos
   * modelos de paralelismo del curso: paralelismo de datos cuando se procesan
   * múltiples itinerarios y paralelismo de tareas cuando se descompone el
   * cálculo interno del tiempo total de un único itinerario.
   */
  def itinerarioSalidaPar(
                           vuelos: List[Vuelo],
                           aeropuertos: List[Aeropuerto]
                         ): (String, String, Int, Int) => Itinerario = {

    val aeropuertosMap: Map[String, Aeropuerto] =
      aeropuertos.map(a => a.Cod -> a).toMap
    val obtenerItinerariosPar = itinerariosPar(vuelos, aeropuertos)

    def offsetMinutos(gmt: Int): Int = (gmt / 100) * 60

    def minutosUTC(hora: Int, minuto: Int, gmt: Int): Int = {
      val totalMinutos = hora * 60 + minuto
      totalMinutos - offsetMinutos(gmt)
    }

    def calcularTiempoTotal(itinerario: Itinerario): Int = {

      def tiempoVuelo(vuelo: Vuelo): Int = {
        val origen = aeropuertosMap(vuelo.Org)
        val destino = aeropuertosMap(vuelo.Dst)
        val salidaUTC = minutosUTC(vuelo.HS, vuelo.MS, origen.GMT)
        val llegadaUTC = minutosUTC(vuelo.HL, vuelo.ML, destino.GMT)
        val tiempo = llegadaUTC - salidaUTC
        if (tiempo < 0) tiempo + 24 * 60 else tiempo
      }

      def tiempoEspera(vueloAnterior: Vuelo, vueloSiguiente: Vuelo): Int = {
        val destinoAnterior = aeropuertosMap(vueloAnterior.Dst)
        val origenSiguiente = aeropuertosMap(vueloSiguiente.Org)
        val llegadaUTC = minutosUTC(vueloAnterior.HL, vueloAnterior.ML, destinoAnterior.GMT)
        val salidaUTC = minutosUTC(vueloSiguiente.HS, vueloSiguiente.MS, origenSiguiente.GMT)
        val espera = salidaUTC - llegadaUTC
        if (espera < 0) espera + 24 * 60 else espera
      }
      
      val (tiempoEnAire, tiempoEnEscala) = parallel(
        itinerario.map(tiempoVuelo).sum,
        itinerario
          .sliding(2)
          .map {
            case List(v1, v2) => tiempoEspera(v1, v2)
            case _ => 0
          }
          .sum
      )

      tiempoEnAire + tiempoEnEscala
    }

    @tailrec
    def calcularDiferenciaSalida(
                                  salidaEnMinutosUTC: Int,
                                  duracionItinerario: Int,
                                  citaEnMinutosUTC: Int
                                ): Int = {
      val llegadaEnMinutosUTC = salidaEnMinutosUTC + duracionItinerario
      if (llegadaEnMinutosUTC <= citaEnMinutosUTC) {
        citaEnMinutosUTC - salidaEnMinutosUTC
      } else {
        calcularDiferenciaSalida(
          salidaEnMinutosUTC - 24 * 60,
          duracionItinerario,
          citaEnMinutosUTC
        )
      }
    }

    def diferenciaItinerario(
                              itinerario: Itinerario,
                              horaCita: Int,
                              minutoCita: Int
                            ): Int = {
      if (itinerario.isEmpty) Int.MaxValue
      else {
        val primerVuelo = itinerario.head
        val ultimoVuelo = itinerario.last
        val origen = aeropuertosMap(primerVuelo.Org)
        val destino = aeropuertosMap(ultimoVuelo.Dst)

        val salidaEnMinutosUTC =
          minutosUTC(primerVuelo.HS, primerVuelo.MS, origen.GMT)

        val citaEnMinutosUTC =
          minutosUTC(horaCita, minutoCita, destino.GMT)

        val duracionItinerario = calcularTiempoTotal(itinerario)

        calcularDiferenciaSalida(
          salidaEnMinutosUTC,
          duracionItinerario,
          citaEnMinutosUTC
        )
      }
    }

    (c1: String, c2: String, h: Int, m: Int) => {
      val todosItinerarios: List[Itinerario] =
        obtenerItinerariosPar(c1, c2)

      if (todosItinerarios.isEmpty) {
        null
      } else {
        val itinerariosConDiferencia: List[(Itinerario, Int)] =
          todosItinerarios
            .par
            .map(it => (it, diferenciaItinerario(it, h, m)))
            .toList

        val mejorItinerario =
          itinerariosConDiferencia.minBy(_._2)._1

        mejorItinerario
      }
    }
  }
}