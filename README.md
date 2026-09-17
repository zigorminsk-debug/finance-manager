# BY Финансы - Финансовый менеджер 🇧🇾

[![Build APK](https://github.com/zigorminsk-debug/finance-manager/actions/workflows/android.yml/badge.svg?branch=arena/01a0b0b8-finance-manager)](https://github.com/zigorminsk-debug/finance-manager/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/zigorminsk-debug/finance-manager?label=Скачать%20APK&color=green)](https://github.com/zigorminsk-debug/finance-manager/releases)

Финансовый менеджер для учёта личных финансов с поддержкой белорусского рубля (BYN) и доллара США (USD) по курсу Национального Банка Республики Беларусь (НБРБ). **APK собирается автоматически через GitHub Actions.**

## 📥 Скачать APK

### Последний релиз:
- **GitHub Releases**: [https://github.com/zigorminsk-debug/finance-manager/releases](https://github.com/zigorminsk-debug/finance-manager/releases)
- **Файлы**:
  - `BY-Finance-Manager-v1.0-debug.apk` (29MB, с логами, для тестирования)
  - `BY-Finance-Manager-v1.0-release.apk` (19MB, оптимизированный)

### Из Actions (последняя сборка):
1. Перейдите в [Actions](https://github.com/zigorminsk-debug/finance-manager/actions/workflows/android.yml)
2. Выберите последний успешный workflow (зелёная галочка ✅)
3. Прокрутите вниз до **Artifacts**
4. Скачайте `BY-Finance-Manager-APKs` (содержит оба APK)

## ✨ Возможности

### 💰 Учёт финансов
- **Приход / Расход / Остаток** - полный учёт в BYN
- **Автоматический пересчёт в USD** по курсу НБРБ на день операции
- Формула: `USD = BYN / курс_НБРБ`
- Курс фиксируется на момент создания операции и сохраняется в базе
- Пример: 100 BYN при курсе 3.25 = 30.77 USD

### 💵 Курс НБРБ
- Автоматическая загрузка курса с официального API: `https://api.nbrb.by/exrates/rates/USD?ondate=YYYY-MM-DD&parammode=2`
- Кэширование курсов для оффлайн работы (SharedPreferences)
- Возможность ручного редактирования курса (если нет интернета)
- Отображение текущего курса на главном экране: `1 USD = X.XXXX BYN`
- Fallback курс 3.2 BYN если API недоступен

### 🏷️ Источники доходов и расходов
**Доходы по умолчанию:**
- 💼 Зарплата, 💻 Фриланс, 🏢 Бизнес, 📈 Инвестиции, 🎁 Подарки, 💰 Прочие доходы

**Расходы по умолчанию:**
- 🛒 Продукты, 🚗 Транспорт, 🏠 Жильё / Коммуналка, ⚕️ Здоровье, 🎮 Развлечения, 👕 Одежда, 🍽️ Кафе / Рестораны, 📱 Связь / Интернет, 📚 Образование, 💸 Прочие расходы

- Создание своих категорий с эмодзи-иконками
- Редактирование и удаление
- Цветовая дифференциация
- Подсчёт по категориям

### 📊 Статистика и фильтры
- Общий баланс в BYN и USD (зелёный если положительный, красный если отрицательный)
- Сумма доходов и расходов отдельно в обеих валютах
- Фильтрация по периоду: Сегодня, Неделя, Месяц, Год, Всё время
- Фильтрация по типу: Доходы / Расходы / Все
- Последние 15 операций на главном экране
- История всех операций с датой, источником, курсом

### 📱 Технические особенности
- **Android**: 7.0+ (API 24+), Target SDK 34
- **База данных**: Room (локальное хранение, без интернета)
- **UI**: Jetpack Compose + Material 3, тёмная и светлая темы
- **Сеть**: Retrofit + OkHttp + Gson для NBRB API
- **Архитектура**: MVVM, Repository, Flow, Coroutines
- **Оффлайн**: Полная работа без интернета, курс берётся из кэша
- **Размер APK**: ~19MB release, ~29MB debug

## 🤖 Сборка APK через GitHub Actions

Проект настроен для **автоматической сборки APK средствами GitHub Actions** — как вы и просили!

### Workflow: `.github/workflows/android.yml`

```yaml
name: Build APK - BY Finance Manager
on:
  push:
    branches: [ "main", "arena/**" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - Checkout
      - Set up JDK 17
      - Setup Android SDK (preinstalled)
      - Build Debug APK: ./gradlew assembleDebug
      - Build Release APK: ./gradlew assembleRelease
      - Upload Artifacts (APKs + logs)

  release:
    needs: build
    steps:
      - Download APKs
      - Create GitHub Release with gh CLI
      - Upload APKs to Release
```

### Как собрать:

1. **Автоматически при пуше** (уже работает!):
   - Любой пуш в ветки `main` или `arena/**` запускает сборку
   - Через 3-5 минут APK появляется в разделе **Actions** → последний workflow → **Artifacts**
   - Также создаётся **Release** с APK файлами

2. **Вручную через GitHub UI**:
   - Перейдите в **Actions** → **Build APK - BY Finance Manager** → **Run workflow**
   - Выберите ветку `arena/01a0b0b8-finance-manager`
   - Нажмите **Run workflow**
   - После завершения (3-5 мин) скачайте APK из Artifacts

3. **Локально** (если есть Android SDK):
   ```bash
   ./gradlew assembleDebug
   # APK будет в app/build/outputs/apk/debug/app-debug.apk
   ./gradlew assembleRelease
   # APK будет в app/build/outputs/apk/release/app-release-unsigned.apk
   ```

### Статус сборки:
- ✅ **Последняя успешная сборка**: Run #13 (35267054515) - 2026-09-17 19:48 UTC
- ✅ **APK размер**: Debug 29MB, Release 19MB
- ✅ **Артефакты**: `BY-Finance-Manager-APKs` (49MB total), `by-finance-manager-debug-apk`, `by-finance-manager-release-apk`
- ✅ **Релиз**: [v1.0.11](https://github.com/zigorminsk-debug/finance-manager/releases/tag/v1.0.11) с APK

## 📂 Структура проекта

```
app/src/main/java/com/byfinancemanager/
├── data/
│   ├── local/          # Room: Transaction, Source, DAOs, Database
│   │   ├── AppDatabase.kt      # Room DB + default sources
│   │   ├── Transaction.kt      # Entity: amountByn, amountUsd, rate, date, sourceId
│   │   ├── Source.kt           # Entity: name, type, icon, color
│   │   └── TransactionType.kt  # INCOME / EXPENSE
│   ├── remote/         # NBRB API
│   │   ├── NbrbApi.kt          # Retrofit: getUsdRateByDate, getCurrentUsdRate
│   │   └── NbrbModels.kt       # NbrbRate model
│   └── repository/     # FinanceRepository
│       └── FinanceRepository.kt # Логика + fetchUsdRateForDate + кэш
├── ui/
│   ├── screens/
│   │   ├── DashboardScreen.kt      # Баланс, курс, последние операции
│   │   ├── TransactionsScreen.kt   # Список с фильтрами
│   │   ├── AddTransactionScreen.kt # Форма: BYN, дата, курс НБРБ, USD, источник
│   │   └── SourcesScreen.kt        # Управление категориями
│   ├── theme/          # Material 3 тема
│   └── viewmodel/
│       └── FinanceViewModel.kt # StateFlow, totals, balance, filters
├── FinanceApp.kt       # Application + DB + Repository
└── MainActivity.kt     # Navigation (BottomNav) + 3 экрана

.github/workflows/
└── android.yml         # Автосборка APK через GitHub Actions
```

## 📖 Использование

1. **Установите APK** на Android устройство (разрешите установку из неизвестных источников)
2. **Добавьте источники**: Перейдите в "Источники" (иконка категории) → нажмите + → создайте свои категории (например: "Такси", "Подработка")
3. **Добавьте операцию**: 
   - Нажмите + (FAB) на главном экране
   - Выберите тип: Доход (зелёный) или Расход (красный)
   - Введите сумму в BYN (например: 100)
   - Выберите дату (курс НБРБ подтянется автоматически с api.nbrb.by)
   - При необходимости отредактируйте курс вручную
   - Посмотрите пересчёт в USD (например: 100 BYN / 3.25 = 30.77 USD)
   - Выберите источник/категорию (например: Продукты)
   - Добавьте заметку (например: "Евроопт")
   - Нажмите Сохранить
4. **Смотрите баланс**: На главной - общий баланс, доходы, расходы в BYN и USD, текущий курс НБРБ
5. **Фильтруйте**: В "Операции" - фильтр по периоду (Сегодня/Неделя/Месяц/Год) и типу (Доходы/Расходы)

## 🌐 API НБРБ

Используется официальный API Национального Банка РБ:

- Текущий курс: `https://api.nbrb.by/exrates/rates/USD?parammode=2`
- Курс на дату: `https://api.nbrb.by/exrates/rates/USD?ondate=2023-06-15&parammode=2`
- По ID валюты (431 = USD): `https://api.nbrb.by/exrates/rates/431?ondate=2023-06-15&parammode=2`

Ответ:
```json
{
  "Cur_ID": 431,
  "Date": "2023-06-15T00:00:00",
  "Cur_Abbreviation": "USD",
  "Cur_Scale": 1,
  "Cur_Name": "Доллар США",
  "Cur_OfficialRate": 3.1234
}
```

Формула в приложении: `USD = BYN / (OfficialRate / Scale)`

## 🛠️ Зависимости

- **Compose BOM**: 2024.02.00 (Compose 1.6.1)
- **Kotlin**: 1.9.22
- **AGP**: 8.2.2
- **Room**: 2.6.1 + KSP 1.9.22-1.0.17
- **Navigation Compose**: 2.7.6
- **Retrofit**: 2.9.0 + Gson
- **OkHttp**: 4.12.0

## 📄 Лицензия

MIT

---

Сделано для Беларуси 🇧🇾 с ❤️ | Полоцк, Витебская область | 2026

**APK готов к установке!** Скачайте из [Releases](https://github.com/zigorminsk-debug/finance-manager/releases) или [Actions Artifacts](https://github.com/zigorminsk-debug/finance-manager/actions/workflows/android.yml)
