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

**(Explicar Aeropuerto, Vuelo y por qué son inmutables.)**

### 3.2. Representación de itinerarios

**(Explicar por qué se usa List[Vuelo].)**

### 3.3. Estructuras auxiliares

**(Maps, Sets u otras colecciones utilizadas.)**

### 3.4. Estructuras paralelas

**(Explicar uso de .par, ParSeq y paralelismo seguro por inmutabilidad.)**

---

## 4. Solución secuencial

### 4.1. Diseño general

**(Explicar recursión, currificación, funciones de orden superior y pattern matching.)**

### 4.2. Funciones implementadas

**(Descripción individual de cada función: qué hace, entradas, salidas y decisiones de diseño.)**

---
    
## 5. Correctitud de las funciones

### 5.1. Correctitud de `itinerarios`

**(Explicar invariante, inducción, exhaustividad y terminación.)**

### 5.2. Correctitud de una función de optimización

**(Elegir Tiempo, Escalas o Aire y demostrar que la selección es correcta.)**

### 5.3. Correctitud de `itinerarioSalida`

**(Explicar filtro correcto, selección del máximo y completitud.)**

---

## 6. Soluciones paralelas

### 6.1. Qué se paralelizó

**(Indicar qué partes del cómputo se ejecutan en paralelo y por qué.)**

### 6.2. Técnicas utilizadas

**(Explicar paralelismo de datos/tareas y uso de colecciones paralelas.)**

### 6.3. Correctitud de la versión paralela

**(Justificar ausencia de carreras y equivalencia con la versión secuencial.)**

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

**(Resumen de aprendizajes, ventajas, limitaciones y posibles mejoras.)**
