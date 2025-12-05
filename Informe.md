# Proyecto Final de Programación Funcional y Concurrente

<center>

Escuela de Ingeniería de Sistemas y Computación

<img src="Images/LogoSimboloUV.png" alt="LogoSimbolo Universidad Del Valle" width="100" height="120">

Profesor Juan Francisco Díaz Frias

Grupo 6:

* 202416285 Arias Rojas Santiago
* 202418564 Bermudez Moreno Isabela
* 202418804 Aragon Alvarez Steven Fernando
* 202416541 González Rosero Andrés Gerardo

Diciembre de 2025
</center>

---

## 1. Introducción

El presente proyecto desarrolla un sistema para la planificación y análisis de itinerarios de vuelo entre aeropuertos, utilizando programación funcional y técnicas de paralelismo en Scala. El objetivo principal es construir un conjunto de funciones capaces de generar rutas válidas entre dos puntos, evaluarlas bajo distintos criterios de optimización y seleccionar las alternativas más convenientes según las restricciones del usuario.

La solución implementada incluye una versión secuencial basada en principios de programación funcional pura —inmutabilidad, funciones de orden superior, recursión y currificación— junto con una versión paralela que aprovecha colecciones paralelas para acelerar la evaluación de itinerarios en conjuntos de datos de mayor tamaño y la paralelización de tareas para hacer que los diferentes trabajos que se deben hacer aprovechen correctamente los recursos disponibles de procesamiento multinucleo y aceleran las mismas tareas. Adicionalmente, se realizó una medición sistemática de rendimiento utilizando ScalaMeter, con el fin de comparar ambas aproximaciones y analizar el impacto real del paralelismo en este tipo de problema.

El informe se organiza presentando primero la descripción formal del problema y las estructuras de datos utilizadas. Posteriormente se detallan las soluciones secuenciales y paralelas, junto con sus respectivos argumentos de correctitud. Finalmente, se exponen los resultados del análisis de desempeño y las conclusiones derivadas del trabajo realizado.

---

## 2. Descripción del problema

El objetivo del proyecto es resolver el problema de planificar itinerarios de vuelo entre dos aeropuertos, a partir de un conjunto de datos que describe aeropuertos y vuelos disponibles. Cada aeropuerto está definido por un código, una ubicación y una zona horaria, mientras que cada vuelo contiene información sobre su aerolínea, número, origen, hora de salida, destino, hora de llegada y escalas técnicas. A partir de estos elementos, un itinerario se define como una secuencia válida de vuelos donde cada destino coincide con el origen del siguiente y ningún aeropuerto se repite.

El sistema debe ser capaz de generar todos los itinerarios posibles entre dos aeropuertos dados y, sobre ellos, aplicar diferentes criterios de optimización según las necesidades del usuario. Entre las consultas a implementar se encuentran: obtener todos los itinerarios viables, seleccionar aquellos con menor tiempo total de viaje, minimizar el número de escalas, minimizar el tiempo en el aire y determinar el itinerario cuya hora de salida sea la más tardía posible que aún permita llegar antes de una cita establecida.

Este problema requiere explorar combinaciones potencialmente numerosas y, por tanto, demanda un enfoque que permita expresar la búsqueda de forma clara y declarativa. A su vez, la naturaleza independiente de muchos cálculos lo hace adecuado para aplicar paralelismo, con el fin de mejorar los tiempos de ejecución en datasets grandes. Por estas razones, la planificación de vuelos se convierte en un escenario ideal para aplicar programación funcional en Scala y evaluar los beneficios de las técnicas paralelas.

---

## 3. Datos y estructuras de datos utilizadas

### 3.1. Case classes

#### En el dominio del problema se modelan dos entidades principales como case class de Scala

* Aeropuerto: representa un nodo en el grafo de conexiones aéreas, con atributos inmutables como código IATA/ICAO, nombre, ciudad y país. Un posible esquema:

  * codigo: String (identificador único)
  * nombre: String
  * ciudad: String
  * pais: String
  * Opcionalmente: coordenadas (lat: Double, lon: Double) si se requiere cálculo de distancia o métrica “Aire”.

* Vuelo: representa una arista dirigida entre aeropuertos, con atributos inmutables que describen la operación del vuelo:

  * origen: Aeropuerto
  * destino: Aeropuerto
  * salida: Instant o LocalDateTime (hora de salida)
  * llegada: Instant o LocalDateTime (hora de llegada)
  * aerolinea: String
  * Opcionalmente: duracion: Duration, numero: String, y métricas como costo o distancia si el criterio de optimización lo requiere.

#### Por qué son inmutables

* Las case class en Scala son por defecto inmutables cuando se definen con val en sus campos y no se exponen setters. Esto es deseable porque:
  * Seguridad en concurrencia: la inmutabilidad evita condiciones de carrera al compartir estructuras entre hilos.
  * Razonamiento y pruebas: funciones puras que reciben y retornan datos inmutables son más fáciles de verificar y depurar.
  * Equivalencia estructural: las case class proveen equals y hashCode derivados de los campos, útil para pertenencia en Set y como claves en Map.
  * Pattern matching: las case class facilitan el deconstructing y el uso de match de forma clara y segura.
  * Si se necesita “modificar” un dato, se usa copy(...), que crea una nueva instancia con los cambios, preservando el original.

#### Inmutabilidad y el dominio

* Un aeropuerto, en el contexto del cálculo de rutas, no cambia durante la ejecución del algoritmo. Similarmente, un vuelo representa un hecho concreto (una conexión con horarios), por lo que su representación debe ser estable. Al mantener ambas entidades inmutables, las operaciones de generación de itinerarios y optimización son referencialmente transparentes.

### 3.2. Representación de itinerarios

Los itinerarios se representan como List[Vuelo]. Esta elección responde a varias razones funcionales y prácticas:

* Orden y secuencia: Un itinerario es una secuencia ordenada de vuelos donde el destino del vuelo i coincide con el origen del vuelo i+1. List preserva orden y facilita operaciones como head, tail, init, last.

* Modelo declarativo: Las operaciones habituales (agregar un vuelo al inicio con ::, concatenar con ++, mapear y filtrar) se expresan naturalmente sobre List.

* Inmutabilidad: List es inmutable, lo cual favorece:
  * compartición estructural eficiente (añadir elementos al frente es O(1)),
  * seguridad en paralelismo y ausencia de efectos colaterales,
  * backtracking funcional en búsquedas recursivas sin necesidad de copiar profundamente.

* Recursión natural: La exploración de itinerarios se implementa de forma recursiva:
  * Caso base: List.empty[Vuelo] representa un itinerario vacío cuando origen = destino o no hay más expansiones válidas.
  * Paso recursivo: construir nuevos itinerarios extendiendo la lista actual con vuelos válidos que respeten conectividad y restricciones de tiempo.

* Compatibilidad con funciones de orden superior: Optimizar por tiempo total, número de escalas o métricas derivadas se formula como plegados (foldLeft/foldRight), mapeos y filtros sobre la lista.

* Corrección y verificabilidad: El invariante “conectividad” y “consistencia temporal” se verifica simple y localmente entre vuelos contiguos en la lista. Además, el uso de List facilita demostrar propiedades por inducción sobre la longitud del itinerario.

### 3.3. Estructuras auxiliares

Además de las case class principales, el sistema utiliza colecciones inmutables para facilitar la búsqueda eficiente y mantener correctitud en escenarios concurrentes:

* Mapas de índice:
  * Map[String, Aeropuerto]: índice por código (IATA/ICAO) para resolver referencias rápidamente al construir o validar vuelos e itinerarios.
  * Map[String, List[Vuelo]]: vuelos salientes por aeropuerto de origen. Este índice permite expandir eficientemente la búsqueda desde un aeropuerto dado sin escanear todos los vuelos.
    * Alternativas: Map[String, Vector[Vuelo]] si se requiere mejor desempeño en acceso aleatorio; Map[String, Seq[Vuelo]] si la fuente de datos es variable pero se mantiene inmutable tras normalización.
* Conjuntos para control de visitas:
  * Set[String] (códigos de aeropuertos visitados): evita ciclos durante la construcción recursiva de itinerarios. Es inmutable y su operación de inclusión (+) crea un nuevo conjunto compartiendo estructura con el anterior.
* Colecciones de resultados:
  * List[List[Vuelo]] o Vector[List[Vuelo]]: conjunto de itinerarios generados antes de una fase de optimización/filtrado. List es adecuado si la construcción es predominantemente por preprend (::). Vector puede ser preferible si hay muchas concatenaciones y acceso por índice.
* Tablas de compatibilidad temporal (opcional):
  * Si se preprocesa la consistencia temporal (ej. ventanas de conexión mínimas), se puede mantener un Map[String, List[Vuelo]] ya filtrado por franja horaria, reduciendo el espacio de búsqueda.
* Tipos derivados:
  * Duration o Long para acumular métricas (tiempo total, número de escalas, distancia "Aire").
  * Ordering[List[Vuelo]] personalizado para comparar itinerarios según el criterio de optimización (por ejemplo, por suma de duraciones o por número de tramos).

Principios de diseño:

* Inmutabilidad: todas estas estructuras se construyen una vez (a partir del dataset) y luego se comparten de forma segura entre funciones y, si aplica, hilos.
* Eficiencia: los Map por origen reducen el branching del árbol de búsqueda; los Set evitan exploraciones redundantes; y la compartición estructural mantiene el costo de backtracking bajo.

### 3.4. Estructuras paralelas

Para aprovechar múltiples núcleos sin comprometer la correctitud, se utilizan colecciones paralelas y patrones de paralelismo seguro:

* Uso de .par y ParSeq:
  * A partir de una colección secuencial (List, Vector, Seq), se obtiene una vista paralela con .par. Ejemplo: val itinerariosPar = itinerariosCandidatos.par.
  * ParSeq distribuye operaciones de orden superior (map, filter, flatMap, fold) sobre múltiples hilos, manteniendo la semántica funcional (sin efectos secundarios).
* Paralelismo de datos:
  * Fases naturalmente paralelizables incluyen:
    * Evaluación de métricas sobre cada itinerario: cálculo de tiempo total, escalas o distancia, con map/fold en paralelo.
    * Filtrado independiente de candidatos: validar consistencia temporal y restricciones de conexión en paralelo con filter.
    * Comparación/selección: aplicar maxBy/minBy paralelos cuando la métrica se puede computar por elemento sin dependencia entre ellos.
* Paralelismo seguro por inmutabilidad:
  * Las estructuras base (Aeropuerto, Vuelo, List[Vuelo], Map, Set) son inmutables, lo que elimina condiciones de carrera al compartirse entre hilos.
  * Las funciones utilizadas son puras (sin writes globales ni IO dentro del cómputo paralelo), garantizando que el resultado paralelo sea determinista y equivalente al secuencial.
* Reducciones y agregaciones:
  * Para operaciones que combinan resultados (ej. buscar el mejor itinerario), se emplean agregaciones con operadores asociativos y sin efectos laterales. Por ejemplo, aggregate o fold en paralelo con una función de combinación que no depende del orden.
* Consideraciones de rendimiento:
  * La conversión a .par tiene overhead; se justifica cuando el número de elementos a procesar es suficientemente grande o cuando cada operación individual es costosa (p. ej., cálculos de distancia, validaciones temporales complejas).
  * Para colecciones pequeñas, la versión secuencial suele ser más rápida.
* Determinismo y equivalencia:
  * Aunque .par puede cambiar el orden de evaluación, se asegura la equivalencia de resultados al usar operaciones insensibles al orden (filtros, mapeos puros, selección por métrica). Si el orden es relevante para la salida, se puede reordenar al final con un sortBy determinista.

En resumen, la elección de .par/ParSeq se concentra en etapas con alto paralelismo de datos y sin dependencias entre elementos, mientras que la inmutabilidad de las estructuras garantiza seguridad y correctitud en la ejecución concurrente.

---

## 4. Solución secuencial

### 4.1. Diseño general

La solución secuencial se implementa de forma declarativa y funcional, apoyándose en recursión, funciones de orden superior y pattern matching sobre listas y case classes. El enfoque evita mutabilidad y efectos secundarios, lo cual facilita el razonamiento y la correctitud.

#### Recorrido del espacio de soluciones con recursión

* La generación de itinerarios se modela como un DFS sobre el grafo de vuelos: se extiende el itinerario parcial con vuelos salientes del aeropuerto actual y se detiene al alcanzar el destino.
* Para evitar ciclos, se mantiene un conjunto inmutable de aeropuertos visitados por rama de búsqueda.
* El caso base devuelve el itinerario acumulado cuando el aeropuerto actual coincide con el destino.

#### Composición mediante funciones de orden superior y for-comprehensions

* La exploración y transformación de colecciones se expresa con for-comprehensions, equivalentes a flatMap y map, permitiendo una composición clara de filtros y expansiones.
* Las funciones de selección y optimización reutilizan la generación general de itinerarios y aplican criterios con map, sortBy, sliding y sum.

#### Pattern matching y deconstrucción de listas

* Para sumar tiempos de espera entre vuelos sucesivos se usa sliding(2), deconstruyendo pares con pattern matching para calcular diferencias de tiempos.
* La estructura del itinerario se trata como una lista ordenada, usando head y last cuando corresponde, y manejando explícitamente casos vacíos.

#### Inmutabilidad y pureza

* Los datos (Aeropuerto, Vuelo, Itinerario como lista de Vuelo) son inmutables; las nuevas alternativas se construyen creando nuevas listas sin modificar las existentes.
* Las funciones no dependen de estado global; reciben parámetros y retornan nuevos valores, favoreciendo transparencia referencial.

#### Normalización de tiempos a UTC

* Cálculos de duración de vuelo y espera entre conexiones se hacen en minutos UTC a partir del GMT del aeropuerto de origen y destino.
* Se corrigen cruces de medianoche sumando 24*60 cuando la llegada en UTC resulte menor que la salida, garantizando métricas consistentes para comparaciones.

En conjunto, el diseño privilegia claridad: se generan todas las rutas válidas de manera recursiva y luego se aplican criterios de optimización con transformaciones funcionales sobre las colecciones resultantes.

### 4.2. Funciones implementadas

Las siguientes funciones están definidas en Itinerarios/package.scala y constituyen la versión secuencial del sistema:

#### itinerarios(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario]

Qué hace: Genera todos los itinerarios posibles entre dos aeropuertos dados mediante DFS, evitando ciclos con un conjunto de visitados.

* Entradas:
  * vuelos: lista de vuelos disponibles.
  * aeropuertos: lista de aeropuertos.
* Salida: función que, dado origen y destino por código, retorna todas las listas de vuelos que conectan origen con destino en orden.
* Decisiones de diseño:
  * Recursión con función interna buscarItinerarios y control inmutable de visitados.
  * Uso de for-comprehensions como azúcar sintáctico de flatMap/map.
  * Caso base cuando el aeropuerto actual coincide con el destino, devolviendo el itinerario acumulado.
  * Construcción de itinerarios sin mutabilidad usando itinerarioActual :+ vuelo.

#### itinerariosTiempo(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario]

Qué hace: Selecciona los tres itinerarios con menor tiempo total de viaje, considerando tiempos en aire y esperas entre conexiones, todo en minutos UTC.

* Entradas:
  * vuelos: lista de vuelos disponibles.
  * aeropuertos: lista de aeropuertos, usada para construir un mapa de acceso por código.
* Salida: función que, dado origen y destino, retorna hasta tres itinerarios con menor tiempo total.
* Decisiones de diseño:
  * Construcción de aeropuertosMap para acceso directo al GMT de cada aeropuerto.
  * Función auxiliar calcularTiempoTotal que:
    * Obtiene tiempo de vuelo por tramo convirtiendo a UTC y corrigiendo cruces de medianoche.
    * Suma esperas entre vuelos consecutivos con sliding(2) y diferencias en UTC, también corrigiendo cruces de día.
    * Retorna tiempo total como suma de tiempo en aire y esperas.
  * Genera todos los itinerarios con itinerarios(...), mapea cada uno a su tiempo total, ordena por sortBy y toma los tres mejores.

#### itinerariosEscalas(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario]

Qué hace: Selecciona los tres itinerarios con menor número total de escalas, combinando escalas técnicas por vuelo y cambios de avión en el itinerario.

* Entradas:
  * vuelos: lista de vuelos.
  * aeropuertos: lista de aeropuertos.
* Salida: función que, dado origen y destino, retorna hasta tres itinerarios con menor cantidad de escalas.
* Decisiones de diseño:
  * Reutiliza itinerarios(...) para obtener todas las rutas válidas.
  * Define numeroEscalas(it) = suma de _.Esc en los vuelos del itinerario más (it.length - 1) por conexiones.
  * Ordena por numeroEscalas y toma los tres primeros, sin mutabilidad.

#### itinerariosAire(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario]

Qué hace: Selecciona los tres itinerarios con menor tiempo total en aire, sin considerar tiempos de espera.

* Entradas:
  * vuelos: lista de vuelos.
  * aeropuertos: lista de aeropuertos.
* Salida: función que, dado origen y destino, retorna hasta tres itinerarios con menor suma de duraciones de vuelo.
* Decisiones de diseño:
  * Construye aeropuertosMap para obtener GMT de origen y destino por vuelo.
  * Define funciones de normalización a UTC y tiempoVuelo con corrección de cruces de medianoche.
  * Define tiempoEnAire(it) como suma de duraciones de los vuelos del itinerario.
  * Ordena por tiempoEnAire y toma los tres mejores.

#### itinerarioSalida(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String, Int, Int) => Itinerario

Qué hace: Selecciona el itinerario que permite salir lo más tarde posible y aun así llegar a una cita en el destino a la hora dada, manejando diferencias horarias y posibles viajes multi-día.

* Entradas:
  * vuelos: lista de vuelos disponibles.
  * aeropuertos: lista de aeropuertos.
* Salida: función que, dado origen, destino y la hora/minuto de la cita en el destino, retorna el itinerario que minimiza la anticipación necesaria respecto a la cita; retorna null si no hay itinerarios.
* Decisiones de diseño:
  * Reutiliza itinerarios(...) para generar todas las rutas.
  * calcularTiempoTotal(it) suma tiempos en aire y esperas en UTC, igual que en itinerariosTiempo.
  * calcularDiferenciaSalida(salidaUTC, duracion, citaUTC) es tail-recursive y resta días completos (24*60) hasta que la llegada sea menor o igual a la hora de la cita, devolviendo la anticipación necesaria en minutos.
  * diferenciaItinerario(it, h, m) calcula la anticipación necesaria por itinerario; si el itinerario está vacío, devuelve un valor centinela grande (Int.MaxValue) para excluirlo.
  * Selecciona el itinerario con mínima anticipación usando minBy, con manejo explícito del caso sin rutas retornando null.

---

## 5. Correctitud de las funciones



---

## 6. Soluciones paralelas

### 6.1. Qué se paralelizó

* Generación de itinerarios
  * Se paralelizó la exploración del grafo de vuelos al convertir la colección de vuelos en una colección paralela y recorrerla con for-comprehensions sobre vuelos.par. Esto permite que la expansión de ramas del DFS se evalúe concurrentemente.
  * Función: itinerariosPar. La recursión y el control de visitados se mantienen inmutables, lo que evita condiciones de carrera.

* Evaluación de métricas sobre los itinerarios
  * Paralelismo de datos: se procesan listas de itinerarios con .par.map para calcular métricas de cada itinerario en paralelo (tiempo total, número de escalas, tiempo en aire).
  * Paralelismo de tareas: dentro del cálculo del tiempo total de un itinerario se paraleliza el cómputo de tiempo en aire y tiempo en escalas con la abstracción parallel(e1, e2).
  * Funciones: itinerariosTiempoPar paraleliza la evaluación del tiempo total por itinerario y divide el cálculo interno en tareas independientes; itinerariosEscalasPar paraleliza el cálculo de la métrica de escalas por itinerario; itinerariosAirePar paraleliza tanto el cálculo de tiempo en aire por itinerario (divide y vencerás con umbrales) como el procesamiento de grandes listas de itinerarios.

* Selección del mejor itinerario para salida tardía
  * Se paraleliza la generación de rutas y la evaluación de la diferencia con la hora de cita por itinerario.
  * Función: itinerarioSalidaPar. Usa .par.map para calcular la diferencia respecto a la cita en paralelo y parallel(e1, e2) dentro del cálculo de tiempo total.

### 6.2. Técnicas utilizadas

* Colecciones paralelas de Scala
  * La conversión a vuelos.par y listas.par habilita el paralelismo de datos de forma declarativa, sin gestionar hilos manualmente.
  * Las operaciones habituales (map, filter, sliding, sum) se ejecutan en paralelo cuando operan sobre colecciones paralelas, repartiendo el trabajo entre múltiples hilos gestionados por la JVM.

* Paralelismo de datos
  * Se aplica al procesar múltiples itinerarios de manera independiente con .par.map. Cada itinerario es una unidad de trabajo sin dependencias, ideal para paralelismo de datos.

* Paralelismo de tareas
  * Se aplica dentro del cálculo del tiempo total de un itinerario, separando tiempo en aire y tiempo en escalas y evaluándolos en paralelo con parallel(e1, e2).
  * En itinerariosAirePar se usa divide y vencerás con umbrales:
    * Si el itinerario supera un umbral de longitud, se divide en dos mitades y se suman sus tiempos en paralelo.
    * Para listas de itinerarios grandes, se divide la lista en dos y se evalúa tiempos en paralelo, concatenando los resultados.

* Inmutabilidad para paralelismo seguro
  * Los datos (Vuelo, Aeropuerto, itinerarios como listas) son inmutables; los conjuntos de visitados son inmutables por rama. Esto evita condiciones de carrera y facilita la equivalencia con la versión secuencial.

* Normalización de tiempos a UTC
  * Todas las métricas de tiempo se calculan en minutos UTC usando GMT de aeropuertos, corrigiendo cruces de medianoche con sumas de 24*60. Esto garantiza consistencia en los criterios aun bajo paralelización.

### 6.3. Correctitud de la versión paralela

* Ausencia de carreras
  * No se utilizan estructuras mutables compartidas entre hilos. Las listas de itinerarios y los conjuntos de visitados se crean y se pasan por valor, manteniendo la inmutabilidad.
  * Las colecciones paralelas operan sobre datos inmutables y retornan nuevos resultados sin efectos laterales, evitando condiciones de carrera.

* Equivalencia semántica con la versión secuencial
  * itinerariosPar sigue el mismo DFS y criterio de evitación de ciclos que la versión secuencial, solo que recorre vuelos en paralelo. El caso base y las extensiones de itinerario son equivalentes.
  * itinerariosTiempoPar calcula exactamente la misma métrica de tiempo total (suma de tiempos en aire y esperas) que itinerariosTiempo; la diferencia es que paraleliza tanto la generación de itinerarios como el cálculo interno de métricas.
  * itinerariosEscalasPar usa la misma fórmula de escalas que itinerariosEscalas, con paralelismo de datos para acelerar la evaluación.
  * itinerariosAirePar mantiene la misma definición de tiempo en aire que itinerariosAire, pero optimiza con paralelización por umbral en itinerarios largos y divide y vencerás en listas grandes.
  * itinerarioSalidaPar reutiliza la misma lógica de tiempo total y diferencia respecto a la cita que la versión secuencial, paralelizando generación y evaluación por itinerario, por lo que su selección del itinerario óptimo es equivalente.

* Manejo correcto de cruces de día y diferencias horarias
  * Las funciones paralelas conservan la normalización a UTC y la corrección de tiempos negativos con sumas de 24*60, garantizando que los cálculos de métricas sean coherentes con la versión secuencial en todos los casos.

* Selección estable y determinista bajo criterios
  * La comparación y ordenamiento por métricas (sortBy) opera sobre valores deterministas calculados funcionalmente. La paralelización no altera el criterio de orden ni el conjunto de candidatos, y la toma de los tres primeros mantiene el mismo resultado que la versión secuencial cuando los datos son iguales.

---

## 7. Evaluación de desempeño

### 7.1. Configuración del experimento

**(Explicar datasets, número de mediciones y uso de ScalaMeter.)**

### 7.2. Resultados

**(Tablas comparando secuencial vs paralelo, incluir speedup.)**

### 7.3. Análisis

**(Interpretar cuándo paralelizar fue útil, cuándo no, y por qué.)**

---

## 8. Conclusiones

El desarrollo de este proyecto permitió evidenciar las ventajas de la programación funcional para la construcción de soluciones declarativas, robustas y fáciles de razonar, especialmente en problemas que involucran búsquedas exhaustivas y composición de funciones. La inmutabilidad y el uso sistemático de funciones de orden superior facilitaron la implementación de algoritmos correctos y libres de efectos colaterales, lo cual resultó fundamental para garantizar la validez de los itinerarios generados y la claridad del código.

La implementación paralela mostró que, en escenarios con conjuntos de datos suficientemente grandes, el uso de colecciones paralelas puede ofrecer mejoras significativas en el tiempo de ejecución. Sin embargo, también se comprobó que en casos pequeños el overhead asociado al paralelismo puede superar sus beneficios, lo que demuestra que las técnicas paralelas deben aplicarse de manera selectiva y justificada, considerando siempre las características del problema y el volumen de datos.

Finalmente, la comparación de desempeño mediante ScalaMeter permitió cuantificar de manera rigurosa el impacto del paralelismo y validar que las versiones paralelas conservan la correctitud funcional de las versiones secuenciales. En conjunto, el proyecto ofrece una experiencia completa en el diseño, análisis y optimización de algoritmos funcionales y paralelos, aportando una comprensión más profunda del paradigma y sus aplicaciones prácticas.

Parece muy importante agregar tambien que en este proyecto hemos podido poner en practica de manera mas profunda los conceptos aprendidos en clase, como la recursión, currificación y el uso de funciones de orden superior, lo cual ha enriquecido nuestra comprensión y habilidades en programación funcional. Todo el curso nos ha servido para entender mejor como pensar en terminos funcionales y como aplicar estos conceptos a problemas reales, lo cual es una habilidad valiosa en el desarrollo de software moderno, podemos incluso aplicar estos conocimientos en entornos de desarrollo multiparadigma, donde la programación funcional viene a complementar otros enfoques, mejorando la calidad y mantenibilidad del código, incluso lo simplifica (como es el caso de las funciones de alto orden, que dejan una solución mas elegante a problemas que antes se resolvian con patrones de diseño como el strategy o visitor). En resumen, este proyecto no solo ha sido una oportunidad para aplicar técnicas de programación funcional y paralela, sino también para consolidar y expandir nuestro entendimiento de estos conceptos fundamentales en la informática moderna.

## 9. Apendices

En esta sección se presentan tablas que resumen el uso de diversas características de programación funcional en las funciones implementadas, incluyendo recursión, reconocimiento de patrones, encapsulación, funciones de alto orden, colecciones, expresiones for, currificación, paralelismo y manejo de tiempos.

### 9.1 Apéndice A — Tabla 1: Uso de la recursión

| Función               | ¿Usa recursión? | Justificación                                             |
| --------------------- | --------------- | --------------------------------------------------------- |
| itinerarios           | Sí              | Implementa buscarItinerarios, que usa recursión de árbol. |
| itinerariosTiempo     | No              | Usa itinerarios y funciones de alto orden.                |
| itinerariosEscalas    | No              | No usa recursión.                                         |
| itinerariosAire       | No              | No usa recursión.                                         |
| itinerarioSalida      | Sí              | Recursión de cola en calcularDiferenciaSalida.            |
| itinerariosPar        | Sí              | Recursión de árbol en buscarItinerariosPar.               |
| itinerariosTiempoPar  | No              | No se llama a sí misma.                                   |
| itinerariosEscalasPar | No              | No se llama a sí misma.                                   |
| itinerariosAirePar    | Sí              | Recursión en tiempoAirePar y tiemposPar.                  |
| itinerarioSalidaPar   | Sí              | Recursión de cola en calcularDiferenciaSalida.            |

### 9.2 Apéndice B — Tabla 2: Reconocimiento de patrones

| Función               | ¿Usa reconocimiento de patrones? | Justificación                                 |
| --------------------- | -------------------------------- | --------------------------------------------- |
| itinerarios           | No                               | No usa match-case.                            |
| itinerariosTiempo     | Sí                               | Usa case List(v1, v2) en calcularTiempoTotal. |
| itinerariosEscalas    | No                               | No usa match-case.                            |
| itinerariosAire       | No                               | No usa match-case.                            |
| itinerarioSalida      | Sí                               | Usa case List(v1, v2) en calcularTiempoTotal. |
| itinerariosPar        | No                               | No usa reconocimiento de patrones.            |
| itinerariosTiempoPar  | Sí                               | Usa case List(v1, v2).                        |
| itinerariosEscalasPar | No                               | No usa match-case.                            |
| itinerariosAirePar    | No                               | No usa match-case.                            |
| itinerarioSalidaPar   | Sí                               | Usa case List(v1, v2).                        |

### 9.3 Apéndice C — Tabla 3: Uso del mecanismo de encapsulación

| Función               | ¿Usa encapsulamiento? | Justificación                                       |
| --------------------- | --------------------- | --------------------------------------------------- |
| itinerarios           | Sí                    | Contiene funciones internas como buscarItinerarios. |
| itinerariosTiempo     | Sí                    | Contiene múltiples funciones auxiliares internas.   |
| itinerariosEscalas    | Sí                    | Usa funciones internas como numeroEscalas.          |
| itinerariosAire       | Sí                    | Usa funciones internas como tiempoEnAire.           |
| itinerarioSalida      | Sí                    | Encapsula cálculos auxiliares internos.             |
| itinerariosPar        | Sí                    | Usa funciones internas como buscarItinerariosPar.   |
| itinerariosTiempoPar  | Sí                    | Usa funciones internas como calcularTiempoTotal.    |
| itinerariosEscalasPar | Sí                    | Define escalasTotales internamente.                 |
| itinerariosAirePar    | Sí                    | Define tiempoEnAirePar y tiemposPar.                |
| itinerarioSalidaPar   | Sí                    | Usa funciones internas.                             |

### 9.4 Apéndice D — Tabla 4: Uso de funciones de alto orden

| Función               | ¿Usa funciones de alto orden? | Justificación                                         |
| --------------------- | ----------------------------- | ----------------------------------------------------- |
| itinerarios           | Sí                            | Retorna funciones y usa transformaciones funcionales. |
| itinerariosTiempo     | Sí                            | Usa map y sortBy.                                     |
| itinerariosEscalas    | Sí                            | Usa map y sortBy.                                     |
| itinerariosAire       | Sí                            | Usa map y sortBy.                                     |
| itinerarioSalida      | Sí                            | Usa map y minBy.                                      |
| itinerariosPar        | Sí                            | Usa operaciones paralelas de orden superior.          |
| itinerariosTiempoPar  | Sí                            | Usa map y sortBy.                                     |
| itinerariosEscalasPar | Sí                            | Usa funciones de orden superior.                      |
| itinerariosAirePar    | Sí                            | tiemposPar recibe funciones como parámetro.           |
| itinerarioSalidaPar   | Sí                            | Usa map.                                              |

### 9.5 Apéndice E — Tabla 5: Uso de colecciones

| Función               | ¿Usa colecciones? | Justificación              |
| --------------------- | ----------------- | -------------------------- |
| itinerarios           | Sí                | Usa List y Set.            |
| itinerariosTiempo     | Sí                | Usa List y Map.            |
| itinerariosEscalas    | Sí                | Usa List.                  |
| itinerariosAire       | Sí                | Usa List y Map.            |
| itinerarioSalida      | Sí                | Usa List y Map.            |
| itinerariosPar        | Sí                | Usa colecciones paralelas. |
| itinerariosTiempoPar  | Sí                | Usa List y Map.            |
| itinerariosEscalasPar | Sí                | Usa List.                  |
| itinerariosAirePar    | Sí                | Usa List y Map.            |
| itinerarioSalidaPar   | Sí                | Usa List y Map.            |

### 9.6 Apéndice F — Tabla 6: Uso de expresiones for

| Función               | ¿Usa expresiones for? | Justificación            |
| --------------------- | --------------------- | ------------------------ |
| itinerarios           | Sí                    | En buscarItinerarios.    |
| itinerariosTiempo     | No                    | No usa for.              |
| itinerariosEscalas    | No                    | No usa for.              |
| itinerariosAire       | No                    | No usa for.              |
| itinerarioSalida      | No                    | No usa for.              |
| itinerariosPar        | Sí                    | En buscarItinerariosPar. |
| itinerariosTiempoPar  | No                    | No usa for.              |
| itinerariosEscalasPar | No                    | No usa for.              |
| itinerariosAirePar    | No                    | No usa for.              |
| itinerarioSalidaPar   | No                    | No usa for.              |

### 9.7 Apéndice G — Tabla 7: Uso de iteradores

| Función               | ¿Usa iteradores? | Justificación                            |
| --------------------- | ---------------- | ---------------------------------------- |
| itinerarios           | Sí               | El for usa iteradores internos.          |
| itinerariosTiempo     | Sí               | Usa map, sortBy, sliding, sum.           |
| itinerariosEscalas    | Sí               | Usa map, sortBy, sum.                    |
| itinerariosAire       | Sí               | Usa map, sortBy, take.                   |
| itinerarioSalida      | Sí               | Usa map, minBy, sum.                     |
| itinerariosPar        | Sí               | Usa iteradores paralelos (splitters).    |
| itinerariosTiempoPar  | Sí               | Usa iteradores paralelos y secuenciales. |
| itinerariosEscalasPar | Sí               | Usa iteradores paralelos.                |
| itinerariosAirePar    | Sí               | Usa map, zip, take.                      |
| itinerarioSalidaPar   | Sí               | Usa operaciones que internamente iteran. |
