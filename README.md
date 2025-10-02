Interstellar Empires
===================

Free, open-source continous multiplayer Real-Time-Strategy game on a big scale. Powered by Java, Scala, LibGDX and Kryonet. It is hackable, moddable and freely playable at [empires.lonelyrobot.io]().

The game was originally developed from 2012 – 2015 and was abandoned due to missing motivation and resources. In the spring of 2017 development was started again, initially focusing on refactoring and cleaning the codebase.

## About this Game

The game is an MMO Real Time Strategy game where players take control of a colony in deep space, take action and make decisions for their people to advance their colony and extend their empire to the stars.

Inspired by early 2000's browser games Interstellar Empires has a focus on macro-scale fleet and economic gameplay. Low level descisions are automated through self-programmable AI in forms of Admirals and Managers while the player is left to develop their strategy in terms of warfare and diplomacy to conquer the stars.

The wiki is currently under re-development but will soon hold more articles about the gameplay.

## Additional Information

The project is currently in a re-structure phase which means that much of the old documentation is out of date and invalid. This README will be updated in the future to reflect changes being made.

## Как собрать и запустить локально

Короткие инструкции для разработчика (на Linux/macOS). В проекте уже добавлен Gradle wrapper (`./gradlew`).

Требования
- JDK 8 — проект компилируется в байткод Java 1.8 (хотя Gradle в контейнере может работать на более новой JVM).
- Подключённый интернет для загрузки зависимостей (пока вы не поместите локальные JAR в `GameFramework/lib`).
- Рекомендуемая IDE: IntelliJ IDEA или Eclipse (импортируйте как Gradle project).

Быстрая сборка
```bash
# из корня репозитория
./gradlew clean build

# собрать только клиентский модуль
./gradlew :ClGLayer:clean :ClGLayer:build
```

Запуск клиента
```bash
# после сборки в папке ClGLayer/build/libs/ появится исполняемый jar (fat-jar)
# пример (проверьте точное имя архива в ClGLayer/build/libs/):
java -jar ClGLayer/build/libs/ClGLayer-0.4a.jar
```

Замечания по зависимостям
- В старой версии проект требовал `net.sourceforge:javaml:0.1.7`, этот артефакт отсутствует в Maven Central. Чтобы сборка не ломалась, в проекте добавлена минимальная локальная реализация `KDTree` (см. `GameFramework/src/net/sf/javaml/core/kdtree/KDTree.java`).
- Если у вас есть оригинальный `javaml-0.1.7.jar`, поместите его в `GameFramework/lib/javaml-0.1.7.jar` — тогда проект будет использовать его вместо локальной заглушки.
- Lombok используется в проекте; в корневом `build.gradle` настроены `compileOnly` и `annotationProcessor` для корректной генерации геттеров/сеттеров при сборке.

Советы по отладке
- Если сборка жалуется на репозитории по HTTP (insecure protocol) — откройте `build.gradle` и включите `allowInsecureProtocol = true` для соответствующего `maven { url = ... }` (только если вы доверяете источнику).
- Если возникает ошибка с нативными библиотеками LibGDX при запуске, убедитесь, что графическая подсистема доступна и запускаете на рабочей машине с GUI (natives-desktop требуется).

Хочете, я добавлю раздел "Run locally" в более подробный `README.developer.md` с инструкциями по IDE и отладке? (могу сделать сейчас)