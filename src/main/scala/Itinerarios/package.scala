import Datos.*

import scala.annotation.tailrec
package object Itinerarios {

  /**
   * Genera todos los itinerarios posibles entre dos aeropuertos dados, modelando
   * el problema como un recorrido en profundidad (DFS) sobre el grafo de vuelos.
   *
   * Cada itinerario se representa como una lista de vuelos `Itinerario`, donde
   * cada vuelo conecta el aeropuerto de origen con el siguiente, y el destino
   * del último vuelo coincide con el aeropuerto destino solicitado.
   *
   * El algoritmo funciona de la siguiente manera:
   *
   * 1. A partir de un aeropuerto de origen `Org`, se consideran todos los vuelos
   * que salen de dicho aeropuerto y cuyo aeropuerto de destino no haya sido
   * visitado aún (para evitar ciclos). El conjunto `visitados` mantiene el
   * historial de aeropuertos ya explorados en la rama actual de búsqueda.
   *
   * 2. Por cada vuelo elegido, se construye un nuevo itinerario parcial
   * (`itinerarioActual :+ vuelo`) y se llama recursivamente a `buscarItinerarios`
   * desde el nuevo aeropuerto de destino de ese vuelo.
   *
   * 3. Cuando el aeropuerto actual `Org` coincide con el destino final `Dst`,
   * se devuelve la lista que contiene el itinerario actual completo. En caso
   * contrario, se sigue explorando recursivamente todas las extensiones
   * posibles a partir de los vuelos salientes disponibles.
   *
   * 4. La función principal `itinerarios` devuelve una función de orden superior
   * `(String, String) => List[Itinerario]` que, dados los códigos de origen
   * y destino (`c1`, `c2`), produce la lista de todos los itinerarios válidos
   * entre dichos aeropuertos.
   *
   * La implementación es puramente funcional: utiliza recursión, conjuntos
   * inmutables para controlar aeropuertos visitados y expresiones `for` como
   * azúcar sintáctico de `flatMap`/`map`, sin recurrir a estructuras mutables
   * ni a bucles imperativos.
   */
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

  /**
   * Selecciona los tres itinerarios entre dos aeropuertos que minimizan el
   * tiempo total de viaje, entendido como el intervalo (en minutos) entre la
   * salida del primer vuelo y la llegada del último, incluyendo tanto el tiempo
   * en aire como los tiempos de espera en las conexiones.
   *
   * El cálculo del tiempo total se realiza de la siguiente manera:
   *
   * 1. Para cada vuelo se calcula su duración en minutos usando aritmética en UTC:
   * las horas locales de salida y llegada se convierten a minutos UTC a partir
   * del GMT de los aeropuertos de origen y destino. Si la llegada en UTC es
   * anterior a la salida (cruce de medianoche), se corrige sumando 24*60.
   *
   * 2. El tiempo de espera entre dos vuelos consecutivos se obtiene como la
   * diferencia (también en UTC) entre la hora de salida del vuelo siguiente
   * y la hora de llegada del vuelo anterior, aplicando el mismo ajuste cuando
   * la diferencia es negativa (esperas que cruzan medianoche).
   *
   * 3. El tiempo total de un itinerario es la suma del tiempo en aire de todos
   * sus vuelos más la suma de los tiempos de espera entre conexiones. Este
   * valor se calcula por la función auxiliar `calcularTiempoTotal`.
   *
   * 4. La función `itinerarios` se reutiliza para generar todos los itinerarios
   * posibles entre `c1` y `c2`. A cada uno se le asocia su tiempo total y la
   * lista resultante se ordena de menor a mayor según esta métrica, tomando
   * finalmente los tres itinerarios con menor tiempo total de viaje.
   *
   * La implementación es completamente funcional, evita estructuras mutables y
   * expresa de forma declarativa el proceso: obtener itinerarios → evaluar tiempo
   * total → ordenar → seleccionar los mejores.
   */
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

  /**
   * Selecciona los tres itinerarios entre dos aeropuertos que presentan el menor
   * número total de escalas, considerando tanto las escalas explícitas indicadas
   * en cada vuelo como los cambios de avión necesarios a lo largo del itinerario.
   *
   * El número de escalas de un itinerario se define como:
   *
   * númeroEscalas(it) = sumatoria(it.map(_.Esc)) + (it.length - 1)
   *
   * donde:
   *   - `_.Esc` representa la cantidad de escalas técnicas asociadas a un vuelo
   *     individual (información proveniente de los datos de entrada).
   *   - `(it.length - 1)` corresponde al número de cambios de avión dentro del
   *     itinerario (un itinerario con n vuelos implica n−1 conexiones).
   *
   * La función opera de manera completamente funcional:
   *
   * 1. Reutiliza la función general `itinerarios` para generar todos los
   * itinerarios posibles entre `origen` y `destino`.
   *
   * 2. Calcula para cada uno su número total de escalas mediante la función
   * auxiliar `numeroEscalas`, definida de manera declarativa y sin mutabilidad.
   *
   * 3. Ordena todos los itinerarios resultantes según esta métrica y selecciona
   * únicamente los tres que presentan el menor número de escalas.
   *
   * El resultado corresponde a los itinerarios más “directos” o con menor carga
   * de conexiones, de acuerdo con el criterio establecido por el enunciado.
   */

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

  /**
   * Selecciona los tres itinerarios entre dos aeropuertos que minimizan el
   * tiempo total en aire, calculado exclusivamente a partir de las horas de
   * vuelo registradas en los datos (horas de salida y llegada ajustadas por
   * la diferencia horaria GMT entre aeropuertos).
   *
   * La función opera de la siguiente manera:
   *
   * 1. Se reutiliza la función general `itinerarios` para generar todos los
   * itinerarios posibles entre los códigos `cod1` y `cod2`, sin emplear
   * estructuras mutables ni lógica imperativa.
   *
   * 2. Para cada vuelo se calcula su duración real en minutos mediante aritmética
   * en UTC: se convierten las horas locales de salida y llegada a minutos UTC
   * usando su correspondiente GMT, corrigiendo automáticamente los cruces de
   * medianoche cuando el tiempo de llegada es menor que el de salida.
   *
   * 3. El “tiempo en aire” de un itinerario es simplemente la suma de las
   * duraciones individuales de todos sus vuelos. Este valor se usa como
   * criterio de ordenamiento.
   *
   * 4. Todos los itinerarios generados para el par (cod1, cod2) se ordenan de
   * menor a mayor tiempo en aire. Luego se retornan únicamente los primeros
   * tres, que representan los itinerarios más eficientes según este criterio.
   *
   * La implementación es completamente funcional, evita mutabilidad, y expresa
   * de manera declarativa el proceso: obtener itinerarios → evaluar su tiempo en
   * aire → ordenar → seleccionar los mejores.
   */

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

  /**
   * Calcula el itinerario óptimo según el criterio de "salida lo más tarde posible"
   * para llegar a una cita en el aeropuerto de destino a la hora indicada.
   *
   * La idea general es la siguiente:
   *
   * 1. Para cada itinerario entre c1 y c2 se calcula su duración total en minutos
   * (sumando tiempos de vuelo y esperas, trabajando siempre en UTC).
   *
   * 2. Se toma la hora de salida del primer vuelo en minutos UTC y, mediante la
   * función `calcularDiferenciaSalida`, se determina cuántos minutos antes de la
   * cita tendría que haber salido dicho itinerario para llegar en o antes de la
   * hora de la cita. Si el itinerario no llega a tiempo en el día base, se asume
   * que debe haberse iniciado uno o más días antes (restando múltiplos de 24h
   * hasta que la llegada sea ≤ cita).
   *
   * 3. El valor resultante (la “anticipación necesaria” en minutos) funciona como
   * criterio de selección: cuanto menor sea este valor, más tarde puede salir
   * el pasajero manteniendo la llegada a tiempo.
   *
   * 4. Finalmente, se escoge el itinerario cuya anticipación necesaria sea mínima.
   * Esto implementa correctamente la idea de “salir lo más tarde posible sin
   * incumplir la cita”, controlando cruces de día y duraciones mayores a 24h.
   *
   * La función es completamente funcional, no usa mutabilidad ni estructuras
   * imperativas, y maneja de forma robusta cruces de medianoche y viajes
   * multi-día mediante aritmética en UTC.
   */

  def itinerarioSalida(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String, Int, Int) => Itinerario = {
    val itinerariosPosibles= itinerarios(vuelos, aeropuertos)
    val aeropuertosMap = aeropuertos.map(a => a.Cod -> a).toMap

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

      val tiempoEnAire = itinerario.map(tiempoVuelo).sum
      val tiempoEnEscala = itinerario.sliding(2).map {
        case List(vueloAnterior, vueloSiguiente) => tiempoEspera(vueloAnterior, vueloSiguiente)
        case _ => 0
      }.sum

      tiempoEnAire + tiempoEnEscala
    }

    @tailrec
    def calcularDiferenciaSalida(salidaEnMinutosUTC: Int,
                                 duracionItinerario: Int,
                                 citaEnMinutosUTC: Int): Int = {
      val llegadaEnMinutosUTC = salidaEnMinutosUTC + duracionItinerario
      if (llegadaEnMinutosUTC <= citaEnMinutosUTC) {
        citaEnMinutosUTC - salidaEnMinutosUTC
      } else {
        calcularDiferenciaSalida(salidaEnMinutosUTC - 24*60,
          duracionItinerario,
          citaEnMinutosUTC)
      }
    }

    def diferenciaItinerario(itinerario: Itinerario,
                             horaCita: Int,
                             minutoCita: Int): Int = {
      if (itinerario.isEmpty) Int.MaxValue
      else {
        val primerVuelo = itinerario.head
        val ultimoVuelo = itinerario.last
        val origen = aeropuertosMap(primerVuelo.Org)
        val destino = aeropuertosMap(ultimoVuelo.Dst)

        val salidaEnMinutosUTC = minutosUTC(primerVuelo.HS, primerVuelo.MS, origen.GMT)
        val citaEnMinutosUTC = minutosUTC(horaCita, minutoCita, destino.GMT)
        val duracionItinerario = calcularTiempoTotal(itinerario)

        calcularDiferenciaSalida(salidaEnMinutosUTC,
          duracionItinerario,
          citaEnMinutosUTC)
      }
    }

    (c1: String, c2: String, h: Int, m: Int) => {
      val todosItinerarios = itinerariosPosibles(c1, c2)
      if(todosItinerarios.isEmpty){
        null
      }
      else{
        val itinerariosConDiferencia = todosItinerarios.map { it =>
          (it, diferenciaItinerario(it, h, m))
        }
        val mejorItinerario = itinerariosConDiferencia.minBy(_._2)._1
        mejorItinerario
      }
    }
  }
}