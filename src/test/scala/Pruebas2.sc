import Itinerarios._
import Datos._
import org.scalameter.{Key, KeyValue, Warmer, config}
import scala.util.Random
import common2._
import ItinerariosPar._
// ==============================
// 5 PRUEBAS PARA CADA FUNCIÓN
// ==============================

println("=" * 80)
println("PRUEBAS ESPECÍFICAS PARA CADA FUNCIÓN CON DATOS DEL CURSO Y ORIGINALES")
println("=" * 80)

// ----------------------------------------------------
// 1. PRUEBAS PARA itinerarios
// ----------------------------------------------------
println("\n" + "=" * 35)
println("1. FUNCIÓN: itinerarios")
println("=" * 35)

val itinCurso = itinerarios(vuelosCurso, aeropuertosCurso)
// val itinOriginal = itinerarios(vuelos, aeropuertos) // COMENTADO: No usar datos originales

// Test 1-1: CLO -> SVO (curso) - Ruta directa con escala técnica en BOG
println("\nTest 1-1: CLO -> SVO (datos curso)")
val resultado1_1 = itinCurso("CLO", "SVO")
println(s"Resultado: ${resultado1_1.length} itinerario(s)")
if (resultado1_1.nonEmpty) {
  println("Itinerario encontrado:")
  resultado1_1.head.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} -> ${v.Dst} (${v.Esc} esc)"))

}

// Test 1-2: CLO -> MEX (curso) - Dos rutas diferentes
println("\nTest 1-2: CLO -> MEX (datos curso)")
println("Descripción: Dos rutas posibles: CLO->BOG->MEX o CLO->BOG->MDE->BAQ->MEX")
val resultado1_2 = itinCurso("CLO", "MEX")
println(s"Resultado: ${resultado1_2.length} itinerario(s) encontrados")
resultado1_2.take(2).zipWithIndex.foreach { case (it, idx) =>
  println(s"  Ruta ${idx+1}: ${it.map(v => s"${v.Org}->${v.Dst}").mkString(" -> ")}")
  println(s"    Vuelos: ${it.map(v => s"${v.Aln}${v.Num}").mkString(", ")}")
}

// Test 1-3: CTG -> PTY (curso) - Directo o con escala
println("\nTest 1-3: CTG -> PTY (datos curso)")
println("Descripción: Directo (COPA1234) o vía SMR (CTG->SMR->PTY)")
val resultado1_3 = itinCurso("CTG", "PTY")
println(s"Resultado: ${resultado1_3.length} itinerario(s)")
if (resultado1_3.nonEmpty) {
  println("Todos los itinerarios:")
  resultado1_3.zipWithIndex.foreach { case (it, idx) =>
    val tipo = if (it.length == 1) "DIRECTO" else s"CON ${it.length-1} ESCALA(S)"
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")} ($tipo)")
  }
}

// Test 1-4: MID -> SVCS (curso) - Ruta bidireccional aislada
println("\nTest 1-4: MID -> SVCS (datos curso)")
println("Descripción: Venezuela aislada del resto, solo tiene vuelos entre MID y SVCS")
val resultado1_4 = itinCurso("MID", "SVCS")
println(s"Resultado: ${resultado1_4.length} itinerario(s)")
if (resultado1_4.nonEmpty) {
  println("Itinerario encontrado:")
  resultado1_4.head.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} -> ${v.Dst}"))
}


// Test 1-5: JFK -> LAX (datos originales) - Ruta directa - COMENTADO
/*
println("\nTest 1-5: JFK -> LAX (datos originales)")
println("Descripción: Buscar rutas entre Nueva York y Los Ángeles")
val resultado1_5 = itinOriginal("JFK", "LAX")
println(s"Resultado: ${resultado1_5.length} itinerario(s) encontrados")
if (resultado1_5.nonEmpty) {
  println("Primeros 2 itinerarios:")
  resultado1_5.take(2).zipWithIndex.foreach { case (it, idx) =>
    println(s"  Itinerario ${idx+1} (${it.length} vuelos):")
    it.foreach(v => println(s"    ${v.Aln}${v.Num}: ${v.Org}->${v.Dst}"))
  }
}
*/

// ----------------------------------------------------
// 2. PRUEBAS PARA itinerariosTiempo
// ----------------------------------------------------
println("\n" + "=" * 35)
println("2. FUNCIÓN: itinerariosTiempo")
println("=" * 35)

val itinTiempoCurso = itinerariosTiempo(vuelosCurso, aeropuertosCurso)
// val itinTiempoOriginal = itinerariosTiempo(vuelos, aeropuertos) // COMENTADO: No usar datos originales

// Test 2-1: CLO -> SVO (curso) - Comparar ruta directa vs por Estambul
println("\nTest 2-1: CLO -> SVO (datos curso)")
println("Descripción: AVA9432 (4 escalas) vs CLO->IST->SVO (3 escalas)")
val resultado2_1 = itinTiempoCurso("CLO", "SVO")
println(s"Resultado: ${resultado2_1.length} mejor(es) itinerario(s) por tiempo")
if (resultado2_1.nonEmpty) {
  resultado2_1.zipWithIndex.foreach { case (it, idx) =>
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
    println(s"     Vuelos: ${it.length}, Escalas técnicas: ${it.map(_.Esc).sum}")
  }
}

// Test 2-2: BOG -> SVO (curso) - Por Madrid (más rápido) vs por Estambul
println("\nTest 2-2: BOG -> SVO (datos curso)")
println("Descripción: IB505+IB506  vs por Estambul")
val resultado2_2 = itinTiempoCurso("BOG", "SVO")
println(s"Resultado: ${resultado2_2.length} mejor(es) itinerario(s) por tiempo")
if (resultado2_2.nonEmpty) {
  println("Mejor itinerario:")
  resultado2_2.head.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org}->${v.Dst} (${v.Esc} esc)"))
}

// Test 2-3: CLO -> MEX (curso) - Comparar dos rutas diferentes
println("\nTest 2-3: CLO -> MEX (datos curso)")
println("Descripción: Comparar tiempo de CLO->BOG->MEX vs CLO->BOG->MDE->BAQ->MEX")
val resultado2_3 = itinTiempoCurso("CLO", "MEX")
println(s"Resultado: ${resultado2_3.length} mejor(es) itinerario(s) por tiempo")
if (resultado2_3.nonEmpty) {
  println("Mejor itinerario por tiempo total:")
  resultado2_3.head.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org}->${v.Dst}"))
  println(s"Número de vuelos: ${resultado2_3.head.length}")
}

// Test 2-4: CTG -> PTY (curso) - Directo vs con escala
println("\nTest 2-4: CTG -> PTY (datos curso)")
println("Descripción: COPA1234 directo (1.5h) vs CTG->SMR->PTY (2 vuelos)")
val resultado2_4 = itinTiempoCurso("CTG", "PTY")
println(s"Resultado: ${resultado2_4.length} mejor(es) itinerario(s) por tiempo")
if (resultado2_4.nonEmpty) {
  resultado2_4.zipWithIndex.foreach { case (it, idx) =>
    val tipo = if (it.length == 1) "DIRECTO" else s"CON ${it.length-1} ESCALA(S)"
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")} ($tipo)")
  }
}

// Test 2-5: JFK -> SFO (datos originales) - COMENTADO
/*
println("\nTest 2-5: JFK -> SFO (datos originales)")
println("Descripción: Mejores 3 itinerarios por tiempo total")
val resultado2_5 = itinTiempoOriginal("JFK", "SFO")
println(s"Resultado: ${resultado2_5.length} mejor(es) itinerario(s) por tiempo")
if (resultado2_5.nonEmpty) {
  resultado2_5.zipWithIndex.foreach { case (it, idx) =>
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
    println(s"     Salida: ${it.head.HS}:${it.head.MS}, Llegada: ${it.last.HL}:${it.last.ML}")
  }
}
*/

// ----------------------------------------------------
// 3. PRUEBAS PARA itinerariosEscalas
// ----------------------------------------------------
println("\n" + "=" * 35)
println("3. FUNCIÓN: itinerariosEscalas")
println("=" * 35)

val itinEscalasCurso = itinerariosEscalas(vuelosCurso, aeropuertosCurso)
// val itinEscalasOriginal = itinerariosEscalas(vuelos, aeropuertos) // COMENTADO: No usar datos originales

// Test 3-1: CLO -> SVO (curso) - AVA9432 (4 escalas) vs por Estambul (3+0)
println("\nTest 3-1: CLO -> SVO (datos curso)")
println("Descripción: Comparar escalas: AVA9432 (4 esc) vs CLO->IST->SVO (3+0 esc)")
val resultado3_1 = itinEscalasCurso("CLO", "SVO")
println(s"Resultado: ${resultado3_1.length} mejor(es) itinerario(s) por menos escalas")
if (resultado3_1.nonEmpty) {
  resultado3_1.zipWithIndex.foreach { case (it, idx) =>
    val escalasTecnicas = it.map(_.Esc).sum
    val cambiosAvion = it.length - 1
    val totalEscalas = escalasTecnicas + cambiosAvion
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
    println(s"     Escalas totales: $totalEscalas ($escalasTecnicas técnicas + $cambiosAvion cambios)")
  }
}

// Test 3-2: BOG -> MEX (curso) - LATAM787 directo (0 escalas)
println("\nTest 3-2: BOG -> MEX (datos curso)")
println("Descripción: LATAM787 es vuelo directo sin escalas técnicas")
val resultado3_2 = itinEscalasCurso("BOG", "MEX")
println(s"Resultado: ${resultado3_2.length} mejor(es) itinerario(s) por menos escalas")
if (resultado3_2.nonEmpty) {
  val it = resultado3_2.head
  val escalasTotales = it.map(_.Esc).sum + it.length - 1
  println(s"Mejor itinerario: ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
  println(s"Escalas totales: $escalasTotales (${it.head.Esc} técnicas + ${it.length-1} cambios)")
  println(s"¿Es LATAM787 directo? ${it.length == 1 && it.head.Num == 787}")
}

// Test 3-3: CLO -> MEX (curso) - Diferentes rutas con diferentes escalas
println("\nTest 3-3: CLO -> MEX (datos curso)")
println("Descripción: Comparar escalas en diferentes rutas a Ciudad de México")
val resultado3_3 = itinEscalasCurso("CLO", "MEX")
println(s"Resultado: ${resultado3_3.length} mejor(es) itinerario(s) por menos escalas")
if (resultado3_3.nonEmpty) {
  println("Top 3 itinerarios por menos escalas:")
  resultado3_3.zipWithIndex.foreach { case (it, idx) =>
    val escalasTotales = it.map(_.Esc).sum + it.length - 1
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
    println(s"     Total escalas: $escalasTotales, Vuelos: ${it.length}")
  }
}

// Test 3-4: MDE -> BAQ (curso) - VIVA769 directo (0 escalas)
println("\nTest 3-4: MDE -> BAQ (datos curso)")
println("Descripción: Vuelo directo entre Medellín y Barranquilla")
val resultado3_4 = itinEscalasCurso("MDE", "BAQ")
println(s"Resultado: ${resultado3_4.length} mejor(es) itinerario(s) por menos escalas")
if (resultado3_4.nonEmpty) {
  val it = resultado3_4.head
  println(s"Mejor itinerario: ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
  println(s"¿Es VIVA769 directo? ${it.length == 1 && it.head.Num == 769}")
}

// Test 3-5: SEA -> MIA (datos originales) - COMENTADO
/*
println("\nTest 3-5: SEA -> MIA (datos originales)")
println("Descripción: Mejores 3 itinerarios por menos escalas Seattle-Miami")
val resultado3_5 = itinEscalasOriginal("SEA", "MIA")
println(s"Resultado: ${resultado3_5.length} mejor(es) itinerario(s) por menos escalas")
if (resultado3_5.nonEmpty) {
  resultado3_5.zipWithIndex.foreach { case (it, idx) =>
    val escalasTotales = it.map(_.Esc).sum + it.length - 1
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
    println(s"     Vuelos: ${it.length}, Escalas totales: $escalasTotales")
  }
}
*/

// ----------------------------------------------------
// 4. PRUEBAS PARA itinerariosAire
// ----------------------------------------------------
println("\n" + "=" * 35)
println("4. FUNCIÓN: itinerariosAire")
println("=" * 35)

val itinAireCurso = itinerariosAire(vuelosCurso, aeropuertosCurso)
// val itinAireOriginal = itinerariosAire(vuelos, aeropuertos) // COMENTADO: No usar datos originales

// Test 4-1: MID -> SVCS (curso) - Vuelo corto (1 hora)
println("\nTest 4-1: MID -> SVCS (datos curso)")
println("Descripción: Vuelo corto de 1 hora (5:00 a 6:00)")
val resultado4_1 = itinAireCurso("MID", "SVCS")
println(s"Resultado: ${resultado4_1.length} mejor(es) itinerario(s) por tiempo en aire")
if (resultado4_1.nonEmpty) {
  val vuelo = resultado4_1.head.head
  val duracion = ((vuelo.HL * 60 + vuelo.ML) - (vuelo.HS * 60 + vuelo.MS) + 1440) % 1440
  println(s"Vuelo: ${vuelo.Aln}${vuelo.Num}")
  println(s"Salida: ${vuelo.HS}:${vuelo.MS}, Llegada: ${vuelo.HL}:${vuelo.ML}")
  println(s"Duración calculada: ${duracion/60}h ${duracion%60}min")
}

// Test 4-2: BOG -> MAD (curso) - Vuelo largo (18:00 a 12:00+1)
println("\nTest 4-2: BOG -> MAD (datos curso)")
println("Descripción: Vuelo nocturno largo (18:00 a 12:00+1 día)")
val resultado4_2 = itinAireCurso("BOG", "MAD")
println(s"Resultado: ${resultado4_2.length} mejor(es) itinerario(s) por tiempo en aire")
if (resultado4_2.nonEmpty) {
  val vuelo = resultado4_2.head.head
  // Cálculo considerando cruce de medianoche
  val duracion = ((vuelo.HL * 60 + vuelo.ML + 1440) - (vuelo.HS * 60 + vuelo.MS)) % 1440
  println(s"Vuelo: ${vuelo.Aln}${vuelo.Num}")
  println(s"Salida: ${vuelo.HS}:${vuelo.MS} BOG, Llegada: ${vuelo.HL}:${vuelo.ML} MAD")
  println(s"Duración estimada: ${duracion/60}h ${duracion%60}min (cruce de día)")
}

// Test 4-3: CTG -> PTY (curso) - Comparar vuelo directo vs con escala
println("\nTest 4-3: CTG -> PTY (datos curso)")
println("Descripción: Comparar tiempo en aire directo (1.5h) vs con escala")
val resultado4_3 = itinAireCurso("CTG", "PTY")
println(s"Resultado: ${resultado4_3.length} mejor(es) itinerario(s) por tiempo en aire")
if (resultado4_3.nonEmpty) {
  println("Mejor itinerario por tiempo en aire:")
  resultado4_3.head.foreach(v => {
    val duracion = ((v.HL * 60 + v.ML) - (v.HS * 60 + v.MS) + 1440) % 1440
    println(s"  ${v.Aln}${v.Num}: ${v.Org} ${v.HS}:${v.MS} -> ${v.Dst} ${v.HL}:${v.ML} (${duracion/60}h${duracion%60}m)")
  })
}

// Test 4-4: CLO -> IST (curso) - Vuelo con escalas técnicas
println("\nTest 4-4: CLO -> IST (datos curso)")
println("Descripción: TURKISH7799 con 3 escalas técnicas (7:00 a 14:00)")
val resultado4_4 = itinAireCurso("CLO", "IST")
println(s"Resultado: ${resultado4_2.length} mejor(es) itinerario(s) por tiempo en aire")
if (resultado4_4.nonEmpty) {
  val vuelo = resultado4_4.head.head
  val duracion = ((vuelo.HL * 60 + vuelo.ML) - (vuelo.HS * 60 + vuelo.MS) + 1440) % 1440
  println(s"Vuelo: ${vuelo.Aln}${vuelo.Num} (${vuelo.Esc} escalas técnicas)")
  println(s"Tiempo en aire: ${duracion/60}h ${duracion%60}min")
}

// Test 4-5: LAX -> JFK (datos originales) - COMENTADO
/*
println("\nTest 4-5: LAX -> JFK (datos originales)")
println("Descripción: Mejores 3 itinerarios por tiempo en aire LAX-JFK")
val resultado4_5 = itinAireOriginal("LAX", "JFK")
println(s"Resultado: ${resultado4_5.length} mejor(es) itinerario(s) por tiempo en aire")
if (resultado4_5.nonEmpty) {
  resultado4_5.zipWithIndex.foreach { case (it, idx) =>
    println(s"  ${idx+1}. ${it.map(v => s"${v.Aln}${v.Num}").mkString(" -> ")}")
    println(s"     Salida: ${it.head.HS}:${it.head.MS}, Llegada: ${it.last.HL}:${it.last.ML}")
  }
}
*/

// ----------------------------------------------------
// 5. PRUEBAS PARA itinerarioSalida
// ----------------------------------------------------
println("\n" + "=" * 35)
println("5. FUNCIÓN: itinerarioSalida")
println("=" * 35)

val itinSalidaCurso = itinerarioSalida(vuelosCurso, aeropuertosCurso)
// val itinSalidaOriginal = itinerarioSalida(vuelos, aeropuertos) // COMENTADO: No usar datos originales

// Test 5-1: BOG -> MAD, cita 14:00 (curso)
println("\nTest 5-1: BOG -> MAD, cita 14:00 (datos curso)")
println("Descripción: Para llegar a Madrid a las 14:00, salir lo más tarde posible")
val resultado5_1 = itinSalidaCurso("BOG", "MAD", 14, 0)
println("Itinerario para salir lo más tarde:")
if (resultado5_1 != null) {
  resultado5_1.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} ${v.HS}:${v.MS} -> ${v.Dst} ${v.HL}:${v.ML}"))
  println(s"Hora de salida: ${resultado5_1.head.HS}:${resultado5_1.head.MS}")
} else {
  println("  No hay itinerario posible")
}

// Test 5-2: CLO -> MEX, cita 20:00 (curso)
println("\nTest 5-2: CLO -> MEX, cita 20:00 (datos curso)")
println("Descripción: Para llegar a Ciudad de México a las 20:00")
val resultado5_2 = itinSalidaCurso("CLO", "MEX", 20, 0)
println("Itinerario para salir lo más tarde:")
if (resultado5_2 != null) {
  resultado5_2.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} ${v.HS}:${v.MS} -> ${v.Dst} ${v.HL}:${v.ML}"))
} else {
  println("  No hay itinerario posible (quizás cita muy temprana)")
}

// Test 5-3: CTG -> PTY, cita 13:00 (curso)
println("\nTest 5-3: CTG -> PTY, cita 13:00 (datos curso)")
println("Descripción: Llegar a Panamá a las 13:00 (después del vuelo directo)")
val resultado5_3 = itinSalidaCurso("CTG", "PTY", 13, 0)
println("Itinerario para salir lo más tarde:")
if (resultado5_3 != null) {
  resultado5_3.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} ${v.HS}:${v.MS} -> ${v.Dst} ${v.HL}:${v.ML}"))
  val llegaAntesCita = resultado5_3.last.HL < 13 || (resultado5_3.last.HL == 13 && resultado5_3.last.ML <= 0)
  println(s"¿Llega antes de las 13:00? $llegaAntesCita")
} else {
  println("  No hay itinerario posible")
}

// Test 5-4: MDE -> BAQ, cita 13:00 (curso)
println("\nTest 5-4: MDE -> BAQ, cita 13:00 (datos curso)")
println("Descripción: Vuelo corto (11:00-12:00), cita después del vuelo")
val resultado5_4 = itinSalidaCurso("MDE", "BAQ", 13, 0)
println("Itinerario para salir lo más tarde:")
if (resultado5_4 != null) {
  resultado5_4.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} ${v.HS}:${v.MS} -> ${v.Dst} ${v.HL}:${v.ML}"))
  // Verificar que es el vuelo directo
  val esVIVA769 = resultado5_4.length == 1 && resultado5_4.head.Num == 769
  println(s"¿Es VIVA769 directo? $esVIVA769")
} else {
  println("  No hay itinerario posible")
}

// Test 5-5: LAX -> JFK, cita 22:00 (datos originales) - COMENTADO
/*
println("\nTest 5-5: LAX -> JFK, cita 22:00 (datos originales)")
println("Descripción: Para llegar a NYC a las 22:00, salir lo más tarde posible desde LA")
val resultado5_5 = itinSalidaOriginal("LAX", "JFK", 22, 0)
println("Itinerario para salir lo más tarde:")
if (resultado5_5 != null) {
  resultado5_5.foreach(v => println(s"  ${v.Aln}${v.Num}: ${v.Org} ${v.HS}:${v.MS} -> ${v.Dst} ${v.HL}:${v.ML}"))
  println(s"Hora de salida desde LAX: ${resultado5_5.head.HS}:${resultado5_5.head.MS}")
} else {
  println("  No hay itinerario posible")
}
*/

println("  Las pruebas con datos originales están comentadas y no se ejecutarán.")