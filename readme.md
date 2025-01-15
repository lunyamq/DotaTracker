# DotaTracker

## Описание
**DotaTracker** — это инструмент для анализа и управления данными по игре Dota 2.
Он предоставляет возможность работы с базами данных MongoDB и SQL, позволяя управлять данными о героях, патчах, игроках и матчах через интерфейс командной строки (CLI).

## Функциональность
- Обновление базы данных героев и патчей из MongoDB.
- Получение информации:
    - О герое по имени.
    - О патче по имени.
    - О данных игрока по ID.
    - О данных матча по ID.
- Удаление данных:
    - Информации об игроке по ID.
    - Информации о матче по ID.

## Установка
1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/lunyamq/DotaTracker.git
   ```
2. Перейдите в директорию проекта:
   ```bash
   cd DotaTracker
   ```
3. Убедитесь, что у вас установлен JDK 21.
4. Соберите проект с использованием Maven
   ```bash
   mvn clean package
   ```

## Зависимости
- MongoDB для работы с данными героев и патчей. 
- SQL для работы с данными игроков и матчей. 
- log4j2 для логирования. 
- Picocli для обработки команд CLI.

## Использование
Приложение предоставляет интерфейс командной строки (CLI) с различными опциями:

### Основные команды
- **Помощь**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -h
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --help
  ```
- **Узнать версию**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -V
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --version
  ```
- **Обновление базы данных**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -r
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --refresh-db
  ```
- **Получение информации о герое**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -hr [HeroName]
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --hero [HeroName]
  ```
- **Получение информации о патче**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -pt [PatchName]
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --patch [PatchName]
  ```
- **Получение информации об игроке**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -p [PlayerID]
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --player [PlayerID]
  ```
- **Удаление информации об игроке**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -pd [PlayerID]
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --player-dell [PlayerID]
  ```
- **Получение информации о матче**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -m [MatchID]
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --match [MatchID]
  ```
- **Удаление информации о матче**:
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar -md [MatchID]
  ```
  ```bash
  java -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar --match-dell [MatchID]
  ```