import Datos._
import Itinerarios._

// Ejemplo curso pequeño
val itsCurso = itinerarios(vuelosCurso,aeropuertosCurso)
//2.1 Aeropuertos incomunicados
val its1 = itsCurso("MID", "SVCS")
val its2 = itsCurso("CLO", "SVCS")

// 4 itinerarios CLO-SVO

val its3 = itsCurso("CLO","SVO")

//2 itinerarios CLO-MEX

val its4 = itsCurso("CLO", "MEX")

//2 itinerarios CTG-PTY
val its5 = itsCurso("CTG","PTY")

val itsTiempoCurso = itinerariosTiempo(vuelosCurso,aeropuertosCurso)

// prueba itinerariosTiempo
val itst1 = itsTiempoCurso("MID", "SVCS")
val itst2 = itsTiempoCurso("CLO", "SVCS")

// 4 itinerarios CLO-SVO

val itst3 = itsTiempoCurso("CLO","SVO")

//2 itinerarios CLO-MEX

val itst4 = itsTiempoCurso("CLO", "MEX")

//2 itinerarios CTG-PTY
val itst5 = itsTiempoCurso("CTG","PTY")

// prueba itinerariosEscalas
val itsEscalasCurso = itinerariosEscalas(vuelosCurso,aeropuertosCurso)

val itsc1 = itsEscalasCurso("MID", "SVCS")
val itsc2 = itsEscalasCurso("CLO", "SVCS")

// 4 itinerarios CLO-SVO

val itsc3 = itsEscalasCurso("CLO","SVO")

//2 itinerarios CLO-MEX

val itsc4 = itsEscalasCurso("CLO", "MEX")

//2 itinerarios CTG-PTY
val itsc5 = itsEscalasCurso("CTG","PTY")

// prueba itinerariosAire
val itsAireCurso = itinerariosAire(vuelosCurso,aeropuertosCurso)

val itsa1 = itsAireCurso("MID", "SVCS")
val itsa2 = itsAireCurso("CLO", "SVCS")

// 4 itinerarios CLO-SVO

val itsa3 = itsAireCurso("CLO","SVO")

//2 itinerarios CLO-MEX

val itsa4 = itsAireCurso("CLO", "MEX")

//2 itinerarios CTG-PTY
val itsa5 = itsAireCurso("CTG","PTY")

// prueba itinerarioSalida
val itSalidaCurso = itinerarioSalida(vuelosCurso,aeropuertosCurso)

val itsal1 = itSalidaCurso("CTG","PTY",11, 40)
val itsal2 = itSalidaCurso("CTG","PTY",11, 55)
val itsal3 = itSalidaCurso("CTG","PTY",10,30)

val its15A1 = itinerarios(vuelosA1,aeropuertos)
val itsTpo15A1 = itinerariosTiempo(vuelosA1,aeropuertos)
val itsEsc15A1 = itinerariosEscalas(vuelosA1,aeropuertos)
val itsAir15A1 = itinerariosAire(vuelosA1,aeropuertos)
val itsSal15A1 = itinerarioSalida(vuelosA1,aeropuertos)
its15A1("HOU","BNA")
itsTpo15A1("HOU","BNA")
itsEsc15A1("HOU","BNA")
itsAir15A1("HOU","BNA")
itsSal15A1("HOU","BNA", 18, 30)

val its40B1 = itinerarios(vuelosB1,aeropuertos)
val itsTpo40B1 = itinerariosTiempo(vuelosB1,aeropuertos)
val itsEsc40B1 = itinerariosEscalas(vuelosB1,aeropuertos)
val itsAir40B1 = itinerariosAire(vuelosB1,aeropuertos)
val itsSal40B1 = itinerarioSalida(vuelosB1,aeropuertos)
its40B1("DFW","ORD")
itsTpo40B1("DFW","ORD")
itsEsc40B1("DFW","ORD")
itsAir40B1("DFW","ORD")
itsSal40B1("DFW","ORD", 18, 30)

val its100C1 = itinerarios(vuelosC1,aeropuertos)
val itsTpo100C1 = itinerariosTiempo(vuelosC1,aeropuertos)
val itsEsc100C1 = itinerariosEscalas(vuelosC1,aeropuertos)
val itsAir100C1 = itinerariosAire(vuelosC1,aeropuertos)
val itsSal100C1 = itinerarioSalida(vuelosC1,aeropuertos)
its100C1("ORD","TPA")
itsTpo100C1("ORD","TPA")
itsEsc100C1("ORD","TPA")
itsAir100C1("ORD","TPA")
itsSal100C1("ORD","TPA", 18, 30)

val its200C = itinerarios(vuelosC1++vuelosC2, aeropuertos)
val itsTpo200C = itinerariosTiempo(vuelosC1++vuelosC2, aeropuertos)
val itsEsc200C = itinerariosEscalas(vuelosC1++vuelosC2, aeropuertos)
val itsAir200C = itinerariosAire(vuelosC1++vuelosC2, aeropuertos)
val itsSal200C = itinerarioSalida(vuelosC1++vuelosC2, aeropuertos)
its200C("ORD","TPA")
itsTpo200C("ORD","TPA")
itsEsc200C("ORD","TPA")
itsAir200C("ORD","TPA")
itsSal200C("ORD","TPA", 18, 30)

/*
val its300C = itinerarios(vuelosC1++vuelosC2++vuelosC3, aeropuertos)
its300C("ORD","TPA")
val its400C = itinerarios(vuelosC1++vuelosC2++vuelosC3++vuelosC4, aeropuertos)
its400C("ORD","TPA")
val its500C = itinerarios(vuelosC1++vuelosC2++vuelosC3++vuelosC4++vuelosC5, aeropuertos)
its500C("ORD","TPA")
*/
