# Walkthrough - Implementação Edge-to-Edge e WindowInsets (Android 15 / SDK 35+)

Implementamos a adequação completa do aplicativo **Rede Duque** para o modo **Edge-to-Edge** obrigatório a partir do Android 15 (SDK 35) e Android 16 (SDK 36), garantindo que barras de sistema (Status Bar, Navigation Bar e recortes de câmera/cutout) não sobreponham os componentes interativos do app.

---

## Modificações Realizadas

### 1. Dependências e Build System

* **[gradle-wrapper.properties](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/gradle/wrapper/gradle-wrapper.properties)**:
  * Atualizado o Gradle Wrapper de `8.2` para `8.7`.
* **[build.gradle](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/build.gradle)**:
  * Remoção total do repositório descontinuado `jcenter()`, utilizando exclusivamente `google()` e `mavenCentral()`.
  * Atualizado o Android Gradle Plugin para `8.4.0` (`classpath 'com.android.tools.build:gradle:8.4.0'`).
  * Atualizado o Kotlin para `1.9.24` (`ext.kotlin_version` e `kotlin-gradle-plugin`), compatível com o AGP `8.4.0` e capaz de ler metadados modernos de bibliotecas como OkHttp e OneSignal.
* **[gradle.properties](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/gradle.properties)**:
  * Adicionado `android.suppressUnsupportedCompileSdk=36` para compatibilidade com SDK 36.
* **[app/build.gradle](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/build.gradle)**:
  * Atualizado `androidx.core:core-ktx` para `1.13.1`.
  * Adicionado `androidx.activity:activity-ktx:1.9.2` (fornecendo `enableEdgeToEdge()`).
  * Fixado `OneSignal:5.1.25` para prevenir incompatibilidade de metadados gerada por builds de range dinâmico.

---

### 2. Layouts XML

* **[activity_intro2.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_intro2.xml)**: Adicionado `android:id="@+id/intro_container"` no `ConstraintLayout` raiz.
* **[activity_login3.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_login3.xml)**: Adicionado `android:id="@+id/nested_scroll_login"` no `NestedScrollView` para controle dinâmico da margem superior com a Toolbar expandida.
* **[activity_webview.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_webview.xml)**: Adicionado `android:id="@+id/main_webview_container"` no `RelativeLayout` raiz.
* **[activity_main.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_main.xml)**: Adicionado `android:id="@+id/main_container"` no `RelativeLayout` raiz.
* **[activity_splash.xml](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/res/layout/activity_splash.xml)**: Adicionado `android:id="@+id/splash_container"` no `RelativeLayout` raiz.

---

### 3. Código das Activities

Em todas as activities, adicionamos `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)` e implementamos o listener `ViewCompat.setOnApplyWindowInsetsListener`:

* **[LoginActivity2.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/LoginActivity2.kt)**:
  * A Toolbar recebe `paddingTop = systemBars.top` e altura ajustada para `systemBars.top + 56dp`. Isso faz com que a barra azul preencha a área da Status Bar de forma fluida, mantendo o botão Voltar e título perfeitamente visíveis.
  * O `NestedScrollView` tem seu `topMargin` ajustado para começar logo abaixo da Toolbar expandida.
  * O container raiz recebe `paddingBottom = systemBars.bottom` e paddings laterais, garantindo que os links de cadastro e botões inferiores não colidam com a barra de gestos ou 3 botões do Android.
* **[IntroActivity2.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/IntroActivity2.kt)**:
  * Insets aplicados no `intro_container` preservando o fundo imersivo `#1f398d`, garantindo margens seguras para logo e botões.
* **[WebViewMainActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/WebViewMainActivity.kt)** & **[MainActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/MainActivity.kt)**:
  * Insets aplicados no container do `WebView`, evitando que cabeçalhos e menus web fiquem atrás da status bar/notch e os rodapés/botões de formulários fiquem sob a barra de navegação.
* **[WebViewActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/WebViewActivity.kt)**:
  * Insets aplicados calculando a altura do ActionBar + Status Bar no topo e barra de navegação no rodapé.
* **[SplashActivity.kt](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/src/main/java/br/com/rededuque/android/SplashActivity.kt)**:
  * `enableEdgeToEdge()` aplicado para transição limpa.

---

## Verificação e Resultados

1. **Compilação Kotlin**:
   * Comando: `./gradlew compileDebugKotlin`
   * Resultado: **BUILD SUCCESSFUL** em 17s sem erros de tipos ou incompatibilidade.
2. **Build Completo do APK**:
   * Comando: `./gradlew assembleDebug`
   * Resultado: **BUILD SUCCESSFUL** em 32s.
   * APK gerado com sucesso.

---

## Correções Adicionais (Sessão 2)

### 4. Compatibilidade Java 17 + Kotlin JVM Target

* **[app/build.gradle](file:///Users/james.martins/Documents/Projects/External/rededuque-app-android/RedeDuqueApp5/app/build.gradle#L46-L52)**:
  * `compileOptions` atualizado para `JavaVersion.VERSION_17` (sourceCompatibility e targetCompatibility).
  * `kotlinOptions.jvmTarget` corrigido de `'1.8'` para `'17'`, eliminando o erro:
    ```
    Inconsistent JVM-target compatibility detected for tasks
    'compileDebugJavaWithJavac' (17) and 'compileDebugKotlin' (1.8)
    ```

### Verificação Final

* Comando: `./gradlew assembleDebug`
* Resultado: **BUILD SUCCESSFUL** em 32s (36 tasks: 10 executed, 26 up-to-date).
* Apenas **warnings** restantes (APIs deprecated como `onBackPressed`, `activeNetworkInfo`, etc.) que não impedem o build nem a execução.

> [!NOTE]
> Os warnings de APIs deprecated (`onBackPressed`, `activeNetworkInfo`, `Handler()`) são recomendações de migração futura e não causam falhas no build ou no runtime.

