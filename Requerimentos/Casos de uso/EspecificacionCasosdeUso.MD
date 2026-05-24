# Especificación de Casos de Uso — Cientific Maxxing

Fecha: 27/03/2026  
Organización: Maxxers Co.  
Proyecto: Cientific Maxxing

---

# Resumen Ejecutivo

Cientific Maxxing es un aplicativo para gestionar experimentos científicos.

## Funcionalidades del Científico
Permite:

- Registrar experimentos
- Cambiar estado de experimentos:
  - Sin comenzar
  - En proceso
  - Exitoso
  - Fallido
- Cargar resultados parciales
- Buscar experimentos
- Consultar resultados de experimentos

## Funcionalidades del Administrador
Permite:

- Buscar científicos
- Agregar científicos
- Modificar científicos
- Eliminar científicos

Su propósito es administrar responsables de experimentos.

---

# Actores

## Científico

**Identificador:** `UsrCientifico`

### Descripción
Usuario principal del sistema. Puede crear, modificar, ver y eliminar experimentos.

### Características
- Tiene nombre e ID autogenerado.
- No requiere login.

### Referencias
Puede:

- Registrar experimentos
- Actualizar estados
- Listar resultados
- Listar experimentos
- Agregar resultados parciales

### Atributos

| Nombre | Descripción | Tipo |
|--------|-------------|------|
| ID | Identifica al científico | Numérico |
| Nombre | Nombre del científico | Alfabético |
| Apellido | Apellido del científico | Alfabético |

---

## Administrador

**Identificador:** `Admin`

### Descripción
Gestiona datos de científicos.

### Características
Accede mediante contraseña de administrador.

### Puede:
- Agregar científicos
- Buscar científicos
- Modificar científicos
- Eliminar científicos

---

# Casos de Uso

---

# RF01 — Registrar Experimento

## Descripción
El científico registra un experimento con:

- ID
- Nombre
- Responsable
- Descripción
- Fecha inicio
- Fecha límite

## Precondición
Debe existir un experimento realizado para registrar.

## Postcondición
El experimento queda guardado en el sistema.

## Flujo principal

1. Sistema muestra formulario:
   - Nombre
   - Descripción
   - Fecha inicio
   - Fecha límite
   - Responsable

2. Científico ingresa datos.

3. Sistema valida formato.

4. Sistema guarda experimento.

5. Se informa registro exitoso.

## Curso alternativo (Paso 3)
Si formato incorrecto:

- Se informa error.
- Se solicita reingresar datos.

---

# RF02 — Agregar Resultado Parcial

## Estados posibles
- Fallido
- Exitoso
- En proceso

## Precondición
Debe existir experimento registrado.

## Flujo principal

1. Sistema solicita:
   - ID experimento
   - Resultado
   - Fecha
   - Prueba realizada

2. Científico ingresa datos.

3. Sistema valida formato.

4. Busca experimento por ID.

5. Verifica estado.

6. Científico agrega resultado parcial.

## Alternativas

### Error formato
- Se informa error.
- Reingresar datos.

### ID inexistente
- Se informa que no existe.

### Experimento finalizado
- No permite modificar resultados.

---

# RF03 — Actualizar Estado Experimento

## Estados
- Fallido
- Exitoso
- En proceso

## Flujo principal

1. Sistema pide:
   - ID experimento
   - Nuevo estado

2. Científico ingresa datos.

3. Validación.

4. Buscar coincidencia.

5. Actualizar estado.

## Alternativas

### Error de formato
Solicita reingreso.

### ID inexistente
Informa que no existe.

---

# RF04 — Listar Resultados de Experimento

## Flujo principal

1. Sistema ofrece filtro por resultado.

2. Científico ingresa filtro.

3. Sistema busca coincidencias.

4. Devuelve listado filtrado.

## Alternativa
Si no hay coincidencias:
- Se informa que no hay resultados.

---

# RF05 — Listar Experimentos

## Precondición
Deben existir experimentos registrados.

## Flujo principal

1. Usuario selecciona listar experimentos.

2. Sistema consulta base de datos.

3. Muestra listado.

## Alternativa
Si no hay experimentos:
- Informar ausencia de registros.

---

# RF06 — Eliminar Científico

## Actores
- Administrador
- Módulo Científico

## Flujo principal

1. Click en eliminar científico.

2. RF10 — Pedir contraseña admin.

3. RF09 — Buscar científico.

4. Eliminar científico.

5. Confirmación exitosa.

---

# RF07 — Agregar Científico

## Precondición
Administrador conoce contraseña.

## Flujo principal

1. Click en agregar científico.

2. RF10 — Pedir contraseña.

3. Sistema solicita datos.

4. Admin carga datos.

5. Validación.

6. Generar ID.

7. Guardar científico.

8. Confirmar alta.

## Alternativa
Formato incorrecto:

- Informar error
- Reingresar datos

---

# RF08 — Modificar Científico

## Nota
La ID no puede modificarse.

## Flujo principal

1. Click modificar científico.

2. RF10 — Pedir contraseña.

3. RF09 — Buscar científico.

4. Ingresar modificaciones.

5. Validar formato.

6. Guardar cambios.

7. Confirmación.

## Alternativa
Si formato incorrecto:
- Informar error
- Reintentar

---

# RF09 — Buscar Científico

## Búsqueda por:
- Nombre
- ID

## Flujo principal

1. Admin ingresa ID.

2. Sistema busca coincidencias.

3. Devuelve resultado.

## Alternativa
Si no encuentra:
- Solicita nueva búsqueda.

---

# RF10 — Pedir Contraseña Admin

## Descripción
Otorga permisos administrativos.

## Flujo principal

1. Sistema solicita código administrador.

2. Admin ingresa contraseña.

3. Sistema valida.

## Alternativa
Si contraseña incorrecta:

- Informa error.
- Solicita reingreso.

---

# Dependencias entre Casos de Uso

- RF06 depende de:
  - RF10
  - RF09

- RF07 depende de:
  - RF10

- RF08 depende de:
  - RF10
  - RF09

---

# Roles del sistema

## Científico
Opera sobre experimentos.

## Administrador
Opera sobre científicos.

---

# Entidades implícitas del dominio

## Experimento
Campos inferidos:

- id
- nombre
- descripcion
- fecha_inicio
- fecha_limite
- estado
- responsable

## ResultadoParcial
- id
- experimento_id
- resultado
- fecha
- prueba

## Científico
- id
- nombre
- apellido
