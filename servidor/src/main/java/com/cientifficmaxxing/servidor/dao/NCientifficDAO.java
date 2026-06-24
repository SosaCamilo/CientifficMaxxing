package com.cientifficmaxxing.servidor.dao;

import com.cientifficmaxxing.servidor.protocolo.Protocolo;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.io.BufferedWriter;
import java.io.FileWriter;
import static java.lang.Thread.sleep;

public class NCientifficDAO {

    // ── Listas ───────────────────────────────────────────────
    public static List<String[]> experimento = new ArrayList<>();
    public static List<String[]> cientifico  = new ArrayList<>();
    public static List<String[]> prueba      = new ArrayList<>();
    public static List<String[]> realiza     = new ArrayList<>();
    public static List<String[]> resultado   = new ArrayList<>();
    public static List<String[]> contrasena   = new ArrayList<>();

    // ── Mapas ────────────────────────────────────────────────
    public static Map<String, String[]> mapaExperimento = new HashMap<>();
    public static Map<String, String[]> mapaCientifico  = new HashMap<>();
    public static Map<String, String[]> mapaPrueba      = new HashMap<>();
    public static Map<String, String[]> mapaRealiza     = new HashMap<>();
    public static Map<String, String[]> mapaResultado   = new HashMap<>();
    
    
    public static String csvExperimento="resources/Experimento.csv";
    public static String csvCientifico="resources/Cientifico.csv";
    public static String csvPrueba="resources/Prueba.csv";
    public static String csvRealiza="resources/Realiza.csv";
    public static String csvResultado="resources/Resultado.csv";
    public static String csvContrasena="resources/Contrasena.csv";
    
    // Mutex para cada archivo, en este orden. (Hay que cambiar las veces que se usa mutex.acquire por esto) 
    
    public static final Semaphore mutexExperimento = new Semaphore(1);
    public static final Semaphore mutexCientifico = new Semaphore(1);
    public static final Semaphore mutexPrueba = new Semaphore(1);
    public static final Semaphore mutexRealiza = new Semaphore(1);
    public static final Semaphore mutexResultado = new Semaphore(1);
    // ── Carga inicial ─────────────────────────────────────────
    public static void cargar() throws IOException {
        experimento = leerCSV("resources/Experimento.csv");
        cientifico  = leerCSV("resources/Cientifico.csv");
        prueba      = leerCSV("resources/Prueba.csv");
        realiza     = leerCSV("resources/Realiza.csv");
        resultado   = leerCSV("resources/Resultado.csv");
        contrasena   = leerCSV("resources/Contrasena.csv");

        for (String[] fila : experimento) mapaExperimento.put(fila[0], fila);
        for (String[] fila : cientifico)  mapaCientifico.put(fila[0], fila);
        for (String[] fila : prueba)      mapaPrueba.put(fila[0], fila);
        for (String[] fila : realiza)     mapaRealiza.put(fila[0] + "," + fila[1], fila);
        for (String[] fila : resultado)   mapaResultado.put(fila[0], fila);
        
        
        
        // Mostar los datos cargados (esto se hace una sola vez) 
                    
        System.out.println("-- Valores Cargados:");
            
        System.out.println("1. Experimento");
        System.out.println("Lista: ");
        mostrarLista(experimento);
        System.out.println("Mapa: ");
        mostrarMapa(mapaExperimento);

        System.out.println("2. Cientifico");
        System.out.println("Lista: ");
        mostrarLista(cientifico);
        System.out.println("Mapa: ");
        mostrarMapa(mapaCientifico);

        System.out.println("3. Prueba");
        System.out.println("Lista: ");
        mostrarLista(prueba);
        System.out.println("Mapa: ");
        mostrarMapa(mapaPrueba);

        System.out.println("4. Realiza");
        System.out.println("Lista: ");
        mostrarLista(realiza);
        System.out.println("Mapa: ");
        mostrarMapa(mapaRealiza);
        System.out.println("5. Resultado");
        System.out.println("Lista: ");
        mostrarLista(resultado);
        System.out.println("Mapa: ");
        mostrarMapa(mapaResultado);
        
        System.out.println("6. Contrasena");
        System.out.println("Lista: ");
        mostrarLista(contrasena);
        
        System.out.println("");
        System.out.println("----- ");
        System.out.println("");
        
    }

    private static List<String[]> leerCSV(String ubicacion) throws IOException {
        List<String[]> lista = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ubicacion))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lista.add(Protocolo.parsearCSV(line));
            }
        }
        return lista;
    }
    public static void mostrarMapa(Map<String, String[]> mapa) {
        for (Map.Entry<String, String[]> entry : mapa.entrySet()) {
            System.out.println(entry.getKey() + " = " + Arrays.toString(entry.getValue()));
        }
    }
    
    public static void mostrarLista(List<String[]> lista) {
        for (String[] fila : lista) {
            System.out.println(Arrays.toString(fila));
        }
    }
    
    
    public int agregarExperimento(String fechaInicio, String fechaFinal, String nombre,
                               String descripcion, String estado, int idResponsable) throws IOException {
        try {
            mutexExperimento.acquire();
        } catch (InterruptedException e) {
            // Vi en google que hacer esto es buena practica
            Thread.currentThread().interrupt(); 
            throw new IOException("Operación interrumpida: " + e.getMessage());
        } 
        try{
            try{
                sleep(5000);
            } catch (InterruptedException e){
                System.err.println("ERROR EN EL SLEEP !!!!!!!!!!!!!!!!!!!!!!!");
            }
            
            asegurarNewlineFinal(csvExperimento); 
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvExperimento, true))) {

                int nuevoId = Integer.parseInt(experimento.get(experimento.size() - 1)[0]) + 1;

                String[] exp1 = {
                    String.valueOf(nuevoId),
                    fechaInicio,
                    fechaFinal,
                    nombre,
                    descripcion,
                    estado,
                    String.valueOf(idResponsable)
                };

                experimento.add(exp1);
                mapaExperimento.put(String.valueOf(nuevoId), exp1);
                
                
                //Thread.sleep(10000);
                String tupla = String.join(",", exp1);
                bw.write(tupla);
                bw.write("\n");
                bw.flush();
                System.out.println("Tupla agregada:");
                System.out.println(tupla);


                return nuevoId; //Devuelve el id (avisa que salio todo bien y pasa un dato util)

            } catch (IOException e) {
                    System.err.println("Error al agregar experimento: " + e.getMessage());
                    return -1;  // Avisa que salio mal sin tirar un error
            } /*catch (InterruptedException e) {
                    System.err.println("Error al agregar experimento: " + e.getMessage());
                    return -1;  // Avisa que salio mal sin tirar un error
            }*/ 
            
        }
        finally {
            mutexExperimento.release();
        }
    }
    
    public int agregarRealiza(int idCientifico, int idExperimento) throws IOException {
        String clave = idCientifico + "," + idExperimento;
        if (mapaRealiza.containsKey(clave)) {
            System.out.println("La relación ya existe: " + clave);
            return -1;// esto le dice a donde se llamo la funcion que ya existia el registro (para no tirar el mismo error que cuando no se pudo)
        }

        try {
            mutexRealiza.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida: " + e.getMessage());
        }
        try{
            asegurarNewlineFinal(csvRealiza);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvRealiza, true))) {
                // se hace por segunda vez por seguridad (capaz que la otra paso simultaneamente entre 2 hilos) 
                // la otra esta porque si hay buen timing me ahorro el mutex
                if (mapaRealiza.containsKey(clave)) {
                    return -1; 
                }

                String[] fila = {
                    String.valueOf(idCientifico),
                    String.valueOf(idExperimento)
                };
                realiza.add(fila);
                mapaRealiza.put(clave, fila);
                bw.write(idCientifico + "," + idExperimento);
                bw.write("\n");
                bw.flush();
                return 1; // Avisa que se hizo todo bien

            } catch (IOException e) {
                throw e; // relanza para que quien llama sepa que fue error real
            } 
        }
        finally {
            mutexRealiza.release();
        }
    }
    // Si el archivo existe y no está vacío y no termina en '\n', agrega un newline para evitar que el próximo registro se pegue al último.
    private static void asegurarNewlineFinal(String path) throws IOException {
        File f = new File(path);
        if (!f.exists() || f.length() == 0) return;

        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(f.length() - 1);
            if (raf.read() != '\n') {
                // El archivo no termina en newline, lo agregamos
                try (FileOutputStream fos = new FileOutputStream(f, true)) {
                    fos.write('\n');
                }
            }
        }
    }
    
    public void actualizarExperimento (int id, String fechaInicio, String fechaFinal, String nombre,
                                       String descripcion, String estado, int idResponsable) throws IOException {
        
        try{
            mutexExperimento.acquire();
            
            //Reemplazar tupla en arraylist
            String[] ntupla= { String.valueOf(id), fechaInicio, fechaFinal, nombre, descripcion, estado, String.valueOf(idResponsable)};
            for (int i=0; i< experimento.size(); i++){
                if (experimento.get(i)[0].equals(String.valueOf(id))){
                    experimento.set(i, ntupla);
                    
                    // Sobreescribir par en mapa
                    mapaExperimento.put(String.valueOf(id), ntupla);
                    
                    // Reescribir un CSV con los valores del ArrayList (Hay que reescribirlo entero para que no queden lineas vacias sueltas)
                    csvUpdater(csvExperimento, experimento);
                    break;
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida: " + e.getMessage());
        }

        
        finally{
                mutexExperimento.release();
        }
    }
    
    private void csvUpdater(String path, List<String[]> array) throws IOException {
        
        //Esta funcion se debe llamar UNICAMENTE cuando el mutex esta acquired (no puede pasar a la vez que otra cosa)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, false))) {
            for (String[] fila : array) {
                bw.write(String.join(",", fila));
                bw.write("\n");
            }
        }
    }
    
    public int agregarResultado(String fecha, String descripcion, String prueba,
                                 int idExperimento, int idPrueba) throws IOException {
        try {
            mutexResultado.acquire();
        } catch (InterruptedException e) {
            // Vi en google que hacer esto es buena practica
            Thread.currentThread().interrupt(); 
            throw new IOException("Operación interrumpida: " + e.getMessage());
        } 
        try{
            // Verifica que se pueda agregar el resultado (logica de negocio, no tiene q ver con concurrencia)
            if ( verificarEstadoExperimento(String.valueOf(idExperimento)) < 0){
                return -1;
            }
            
            // Comienza a escribir
            asegurarNewlineFinal(csvResultado);
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvResultado, true))) {

                int nuevoId = Integer.parseInt(resultado.get(resultado.size() - 1)[0]) + 1;

                String[] res1 = {
                    String.valueOf(nuevoId),
                    fecha,
                    descripcion,
                    prueba,
                    String.valueOf(idExperimento),
                    String.valueOf(idPrueba)
                };

                resultado.add(res1);
                mapaResultado.put(String.valueOf(nuevoId), res1);

                String tupla = String.join(",", res1);
                bw.write(tupla);
                bw.write("\n");
                bw.flush();
                System.out.println("Tupla agregada:");
                System.out.println(tupla);


                return nuevoId; //Devuelve el id (avisa que salio todo bien y pasa un dato util)

            } catch (IOException e) {
                    throw e;
            } 
            
        }
        finally {
            mutexResultado.release();
        }
    }
    private int verificarEstadoExperimento(String idExperimento) {
        // Esta func solo se llama dentro de un semaforo
        String[] tupla = mapaExperimento.get(idExperimento);
        if (tupla == null) return -1;
        
        System.out.println(Arrays.toString(tupla));
        
        if (!tupla[5].equalsIgnoreCase("En Proceso")) {
            System.err.println("El experimento no esta en proceso.");
            return -1;
        }
        return 1;
    }
    /*
    public List<String[]> listarExperimentos (){
        return experimento;
    }*/
    
    public static List<String[]> listarExperimentos() {
        try {
            mutexExperimento.acquire();
        } catch (InterruptedException e) {
            // Vi en google que hacer esto es buena practica
            Thread.currentThread().interrupt(); 
            //throw new IOException("Operación interrumpida: " + e.getMessage());
        } try{
            List<String[]> lista = new ArrayList<>();
            lista.add(new String[]{"IdExperimento", "Nombre", "Descripcion", "FechaInicio", 
                                   "FechaFinal", "Estado", "Responsable", "IdResponsable", "Equipo"});

            for (String[] exp : experimento) {
                // exp: [0]=Id [1]=FechaInicio [2]=FechaFinal [3]=Nombre [4]=Desc [5]=Estado [6]=IdResp

                // buscar nombre del responsable en cientifico
                String idResp = exp[6];
                String nombreResp = "";
                String[] cien = mapaCientifico.get(idResp);
                if (cien != null) nombreResp = cien[1] + " " + cien[2]; // Nombre + Apellido

                // buscar equipo en realiza
                StringBuilder equipo = new StringBuilder();
                for (String[] r : realiza) {
                    if (r[1].equals(exp[0])) { // r[1]=IdExperimento
                        String[] c = mapaCientifico.get(r[0]); // r[0]=IdCientifico
                        if (c != null) {
                            if (equipo.length() > 0) equipo.append(", ");
                            equipo.append(c[1]).append(" ").append(c[2]);
                        }
                    }
                }

                lista.add(new String[]{
                    exp[0],          // IdExperimento
                    exp[3],          // Nombre
                    exp[4],          // Descripcion
                    exp[1],          // FechaInicio
                    exp[2],          // FechaFinal
                    exp[5],          // Estado
                    nombreResp,      // Responsable (texto)
                    idResp,          // IdResponsable
                    equipo.toString() // Equipo
                });
            }
            return lista; } 
        finally {
            mutexExperimento.release();
        }
    }
    
    
    /*public List<String[]> listarCientificos (){
        return cientifico;
    }*/
    
    /*public List<String[]> listarResultadosPorExperimento(int id){
        
        List<String[]> resultadoExp   = new ArrayList<>();
        for (String[] fila : resultado){
            if (Integer.parseInt(fila[4]) == id && fila[4].equals(String.valueOf(id))){
                resultadoExp.add(fila);
            }
        }
        return resultadoExp;
    } */
    
    public static List<String[]> listarResultadosPorExperimento(int id) {
        try {
            mutexResultado.acquire();
        } catch (InterruptedException e) {
            // Vi en google que hacer esto es buena practica
            Thread.currentThread().interrupt(); 
            //throw new IOException("Operación interrumpida: " + e.getMessage());
        } try{
            List<String[]> lista = new ArrayList<>();
            lista.add(new String[]{"IdResultado", "Fecha", "Descripcion", "Prueba", 
                                   "TipoDePrueba", "Experimento"});

            for (String[] res : resultado) {
                
                // resultado :[0]=IdResultado [1]=Fecha [2]=Descripcion [3]=Prueba [4]=IdExperimento [5]=IdPrueba
                
                
                // Joins Hechos: resultadoExp : [0]=IdResultado [1]=Fecha [2]=Descripcion [3]=Prueba [4]=TipoDePrueba [5]=Experimento (nombre del experimento)
                 
                
                // Solo avanza si es del experimento con id = la variable id
                
                if (Integer.parseInt(res[4]) == id && res[4].equals(String.valueOf(id))){
                    
                    // buscar nombre del responsable en cientifico
                    String idExp = res[4];
                    String nombreExp = "";

                    String idPrueba = res[5];
                    String nombrePrueba = "";
                    
                    
                    String[] aux = mapaExperimento.get(idExp);
                    if (aux != null) nombreExp = aux[3]; // Si existe el experimento, asigna la variable nombreExp al nombre del mismo

                    aux = mapaPrueba.get(idPrueba);
                    if (aux != null) nombrePrueba = aux[1]; // Si existe la prueba, asigna la variable nombrePrueba al nombre de la misma

                    
                    

                    lista.add(new String[]{
                        res[0],          // IdResultado
                        res[1],          // Fecha
                        res[2],          // Descripcion
                        res[3],          // Prueba
                        nombrePrueba,          // TipoDePrueba
                        nombreExp          // Experimento
                    });
                }
            }
            return lista; } 
        finally {
            mutexResultado.release();
        }
    }
    
    public static List<String[]> listarCientificos() {
        try {
            mutexCientifico.acquire();
        } catch (InterruptedException e) {
            // Vi en google que hacer esto es buena practica
            Thread.currentThread().interrupt(); 
            //throw new IOException("Operación interrumpida: " + e.getMessage());
        } try{
            List<String[]> lista = new ArrayList<>();
            lista.add(new String[]{"IdCientifico", "Nombre", "Apellido", "Nacimiento"});

            for (String[] cien : cientifico) {
                // cien: [0]=IdCientifico [1]=Nombre [2]=Apellido [3]=Nacimiento 

                lista.add(cien);
            }
            return lista; } 
        finally {
            mutexCientifico.release();
        }
    }
    public static String obtenerContraseniaAdmin(){
        return contrasena.get(0)[1];
    }
    
    public void actualizarCientifico(int id, String nombre, String apellido, String nacimiento)
            throws IOException {
        
        try{
            mutexCientifico.acquire();
            
            //Reemplazar tupla en arraylist
            String[] ntupla= { String.valueOf(id), nombre, apellido, nacimiento};
            for (int i=0; i< cientifico.size(); i++){
                if (cientifico.get(i)[0].equals(String.valueOf(id))){
                    cientifico.set(i, ntupla);
                    
                    // Sobreescribir par en mapa
                    mapaCientifico.put(String.valueOf(id), ntupla);
                    
                    // Reescribir un CSV con los valores del ArrayList (Hay que reescribirlo entero para que no queden lineas vacias sueltas)
                    csvUpdater(csvCientifico, cientifico);
                    break;
                }
            }} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida: " + e.getMessage());
        }finally{
                mutexCientifico.release();
        }
    }
    /*
    public void actualizarCientifico(int id, String nombre, String apellido, String nacimiento)
            throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_ActualizarCientifico(?,?,?,?)}")) {
            cs.setInt(1, id);
            cs.setString(2, nombre);
            cs.setString(3, apellido);
            cs.setDate(4, parseFecha(nacimiento));
            cs.execute();
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —actualizar científico: " + e.getMessage());
            throw e;
        }
    }
    */
} 


