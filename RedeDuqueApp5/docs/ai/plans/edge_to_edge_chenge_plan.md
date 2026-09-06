# Adequação ao Modo Edge-to-Edge (Android 15 / SDK 35 e SDK 36)

O Android 15 (API 35) e Android 16 (API 36) tornam o modo **Edge-to-Edge** obrigatório por padrão para apps que visam o `targetSdkVersion >= 35`. O app atual (`RedeDuqueApp5`) tem `targetSdkVersion 36`, mas nenhuma Activity ou layout trata `WindowInsets`. Isso faz com que a barra de status (ícones de bateria, relógio, notch de câmera) e a barra de navegação/gestos sobreponham o topo e rodapé de telas vitais, como o login, webviews e menus.

Este plano define as etapas necessárias para atualizar as dependências do projeto e aplicar o tratamento correto de `WindowInsets` via `enableEdgeToEdge()` e `ViewCompat.setOnApplyWindowInsetsListener`.

---

## User Review Required

> [!IMPORTANT]
> **Compatibilidade de Kotlin / Gradle**:
> O projeto foi atualizado para:
> - **Gradle Wrapper**: `8.7`
> - **Android Gradle Plugin (AGP)**: `8.4.0`
> - **Kotlin**: `1.9.24`
> - **Repositórios**: Remoção de `jcenter()`, mantendo `google()` e `mavenCentral()`.
> - **AndroidX**: `androidx.activity:1.9.2` e `androidx.core:1.13.1`.

---

## Proposed Changes

### 1. Configuração e Dependências

#### [MODIFY] [build.gradle](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/build.gradle)
- Remover `jcenter()` e manter `google()` e `mavenCentral()`.
- Atualizar `classpath 'com.android.tools.build:gradle:8.4.0'`.
- Atualizar `ext.kotlin_version = '1.9.24'` e `classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24'`.

#### [MODIFY] [gradle-wrapper.properties](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/gradle/wrapper/gradle-wrapper.properties)
- Atualizar `distributionUrl` para `gradle-8.7-bin.zip`.

#### [MODIFY] [gradle.properties](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/gradle.properties)
- Adicionar `android.suppressUnsupportedCompileSdk=36`.

#### [MODIFY] [app/build.gradle](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/build.gradle)
- Atualizar `androidx.core:core-ktx` para `1.13.1`.
- Adicionar `androidx.activity:activity-ktx:1.9.2` para fornecer a API padrão `enableEdgeToEdge()`.
- Fixar `OneSignal:5.1.25`.

---

### 2. Layouts XML (Identificadores de Container)

#### [MODIFY] [activity_intro2.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_intro2.xml)
- Adicionar `android:id="@+id/intro_container"` no `ConstraintLayout` raiz para vincular o listener de insets.

#### [MODIFY] [activity_webview.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_webview.xml)
- Adicionar `android:id="@+id/main_webview_container"` no `RelativeLayout` raiz.

#### [MODIFY] [activity_main.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_main.xml)
- Adicionar `android:id="@+id/main_container"` no `RelativeLayout` raiz.

---

### 3. Código das Activities (Aplicação de Edge-to-Edge e WindowInsets)

#### [MODIFY] [LoginActivity2.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/LoginActivity2.kt)
- Chamar `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` no container raiz (`login_container`):
  - Insets superiores (`statusBars() or displayCutout()`): aplicados na Toolbar para que a barra colorida preencha até o topo físico, mas os botões de voltar e título fiquem abaixo dos ícones do sistema e câmera.
  - Insets inferiores (`navigationBars()`): aplicados como padding no container da tela para evitar que os links e botões inferiores fiquem cobertos pela barra de navegação/gestos.

#### [MODIFY] [IntroActivity2.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/IntroActivity2.kt)
- Chamar `enableEdgeToEdge()`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` no `intro_container` aplicando insets de `systemBars() or displayCutout()`, preservando o fundo azul total e garantindo espaçamento de botões e logo.

#### [MODIFY] [WebViewMainActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/WebViewMainActivity.kt)
- Chamar `enableEdgeToEdge()`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` no `main_webview_container` garantindo que o `WebView` receba padding superior e inferior correspondentes à barra de status e barra de navegação.

#### [MODIFY] [WebViewActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/WebViewActivity.kt)
- Chamar `enableEdgeToEdge()`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` no container raiz (`login_container`) para evitar sobreposição da barra de navegação inferior.

#### [MODIFY] [MainActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/MainActivity.kt)
- Chamar `enableEdgeToEdge()`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` no container raiz (`main_container`).

#### [MODIFY] [SplashActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/SplashActivity.kt)
- Chamar `enableEdgeToEdge()` para transição visual suave até a `IntroActivity2`.

---

## Verification Plan

### Automated / Build Tests
- Executar `./gradlew compileDebugKotlin` para garantir que todas as classes Kotlin compilam sem erros de incompatibilidade.
- Executar `./gradlew assembleDebug` para validar a geração completa do APK.

### Manual Verification
- Testar o comportamento das telas em emulador com Android 15 (API 35) ou superior, validando se:
  1. O topo de `LoginActivity2` (botão voltar e título) não colide com relógio e bateria.
  2. O conteúdo das páginas web em `WebViewMainActivity` não fica oculto sob a barra de navegação ou barra de status.
  3. Os botões de `IntroActivity2` permanecem com margem segura da barra de gestos inferior.
