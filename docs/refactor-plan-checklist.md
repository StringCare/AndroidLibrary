# Refactor plan checklist (plugin / repo)

Estado al **2026-03-19**. Marca lo implementado en el código actual; úsalo para seguimiento del roadmap.

Leyenda: **`[x]`** hecho · **`[ ]`** pendiente / parcial · **`[~]`** hecho con matices (ver nota)

---

## Fase 0 — Setup y calidad base

- [x] Rama / flujo de trabajo acordado para cambios grandes del plugin
- [x] Detekt en `plugin/build.gradle.kts` + `config/detekt/detekt.yml` + **baseline** (`baseline.xml`)
- [x] Ktlint en el plugin (`ignoreFailures`, exclusión legacy `Vars.kt`)
- [x] Develocity / build scan en `settings.gradle.kts` (publicación condicional a fallos)
- [x] Tests JUnit 5 por defecto; tarea `:plugin:integrationTest` + `@Tag("integration")` para clones/nested Gradle

---

## Fase 1 — Seguridad y ejecución

- [x] Estado mutable del plugin migrado a **`StringCareBuildService`** + `StringCareSession` solo para tests
- [x] Tareas críticas con **`usesService`** al build service
- [x] Comandos shell vía **`ProcessBuilder`** (no shell inseguro directo en el path feliz)
- [x] Timeout (~60s) y **`ExecutionResult.Timeout`**
- [x] JNI del host: verificación **SHA-256** antes de cargar, reintentos, **`LoadResult`**, limpieza de temporales JNI (sin `deleteOnExit` como única estrategia)

---

## Fase 2 — Rendimiento y XML

- [x] Parser **SAX-first** para `strings.xml` + **fallback DOM** si hay anidamiento / detector falla
- [x] Fachada `XmlParser` / `StringsXmlParser`; `XParser` delega
- [x] Walk de recursos con **prune** de `build/`, `.gradle/`, `.git/`, `node_modules/`
- [x] `StringCareConfiguration.normalize()` idempotente
- [x] Copia de backup **temp + move atómico** (`BackupCopy` + `ResourceFile` / `AssetsFile`)
- [ ] Módulo **JMH** u otro benchmark reproducible para el parser
- [ ] Regresión de rendimiento automatizada en CI

---

## Fase 3 — APIs Gradle

- [x] Entradas de tareas como **`Property<>`** donde aplica
- [x] **`@DisableCachingByDefault`** en tareas que mutan el árbol de fuentes (no `@CacheableTask`)
- [x] Wiring con AGP: dependencias de merge/process/assets dentro de `onVariants {}` usando `tasks.matching { it.name == ... }.configureEach { ... }` (sin `afterEvaluate` ni `findByName`)
- [x] Sustituido `afterEvaluate` por wiring lazy y seguro por nombre de tarea
- [ ] Declarar **`@InputFiles` / outputs** fiables para tareas mutadoras (difícil mientras se editan fuentes in-place)

---

## Fase 4 — Arquitectura (capas)

- [x] Modelos en `domain.models` (`StringEntity`, `ResourceFile`, `AssetsFile`, `ExecutionResult`, `Backupable`, etc.)
- [x] `infrastructure`: parsers, filesystem (`BackupService`), crypto (`ObfuscationService`), Gradle (`FileSystemResourceRepository`)
- [x] Casos de uso / repositorios (`ObfuscateStringsUseCase`, `ResourceRepository`)
- [x] AGP como **`compileOnly`** en el plugin
- [x] JSON de listas con **kotlinx-serialization** (sin Gson)
- [x] **`ExtractFingerprintUseCase`** en `domain.usecases` (tests pueden llamar `extract` con `log` por defecto; `extractFingerprint` delega desde `internal`)
- [ ] Partir **`Extensions.kt`** en módulos más pequeños (Gradle vs FS vs XML)

---

## Fase 5 — Kotlin y robustez

- [x] Reducción de **`!!`** en fuentes principales del plugin (revisar periódicamente)
- [x] Utilidades tipo `toReadableString` donde aplica
- [~] API de ejecución: ahora **`execute(command: String)` → `Result<ExecutionResult>`** y existe overload con **`List<String>`**; queda como deuda dejar `List<String>` como API única

---

## Fase 6 — Tests y cobertura

- [x] Tests unitarios por defecto **sin** integración pesada (`:plugin:test` excluye tag `integration`)
- [x] JaCoCo: `jacocoTestReport` + **`jacocoTestCoverageVerification`** (umbral **bajo interino** ~12% líneas; objetivo roadmap **>80%**)
- [x] Tests de infraestructura XML (`XmlParserInfrastructureTest`: SAX, DOM implícito, **UTF-16**, XML mal formado)
- [x] `ObfuscationServiceTest` (roundtrip JNI con **`assumeTrue(Stark.isNativeLibLoaded() is LoadResult.Loaded)`)**
- [x] `BackupServiceTest` vía `BackupService` + `defaultConfig()`
- [x] Otros tests de configuración / JSON / fingerprint según el árbol actual (`ConfigurationNormalizationTest`, `FingerprintExtractionTest`, `JsonListsTest`, etc.)
- [ ] Fixtures **`test-projects/`** minimalistas adicionales (además de samples existentes)
- [ ] Cobertura **>80%** como gate duro en CI

---

## Fase 7 — Mantenibilidad

- [x] Detekt con baseline (no “cero issues” sin baseline)
- [ ] Subir poco a poco reglas / reducir baseline hasta acercarse a **Detekt sin baseline**
- [x] Ktlint operativo; fallos no bloquean build (`ignoreFailures`)
- [~] KDoc: presente en puntos clave; ampliar en APIs públicas internas del plugin

---

## Fase 8 — Documentación y CI/CD

- [x] `CHANGELOG.md` actualizado con hitos del refactor
- [x] `MIGRATION.md` / docs de migración
- [x] `SECURITY.md` y documentación de contribución / build
- [x] `plugin/README.md` (requisitos, DSL avanzado, troubleshooting, rendimiento)
- [x] **`.github/workflows/ci.yml`**: test, JaCoCo report + **verification**, detekt, integration opcional, `assembleDebug`
- [x] **`.github/workflows/publish.yml`**: esqueleto (build JAR; publicación manual documentada)
- [ ] **OWASP Dependency-Check** (o equivalente) en Gradle y/o CI del plugin
- [ ] **Matriz CI** (p.ej. varias versiones AGP / Gradle) donde tenga sentido para el composite build

---

## Resumen rápido

| Área              | Estado |
|-------------------|--------|
| BuildService / JNI / ProcessBuilder | Listo |
| XML SAX + DOM     | Listo |
| Backup atómico    | Listo (sin locks ni dirs UUID dedicados) |
| AGP wiring        | Estable con `onVariants` + `tasks.matching(...).configureEach` |
| Tests + JaCoCo    | Listo con umbral bajo; subir cobertura |
| OWASP / JMH / matriz AGP | Pendiente |

---

*Si el plan original vive fuera del repo (p.ej. adjunto en Cursor), copia aquí los IDs `phase-*` y alinéalos con las secciones anteriores.*
