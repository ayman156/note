# 📝 Note A - تطبيق الملاحظات الذكي والمتكامل

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-Offline_First-009688?style=for-the-badge&logo=sqlite&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**تطبيق ملاحظات احترافي وعصري مبني بأحدث تقنيات أندرويد لعام 2026**
*(Jetpack Compose, Material 3, Clean Architecture, Room Database, Biometric Security)*

</div>

---

## 🌟 نبذة عن التطبيق (Overview)

**Note A** هو تطبيق أندرويد متطور لكتابة وتنظيم الملاحظات والأفكار بكل سلاسة وسرعة، مصمم وفق مبادئ **Material Design 3** الأنيقة ومجهز بمحرك نصوص قوي لا يضع أي حدود أو قيود على حجم النص المكتوب. يجمع التطبيق بين البساطة الفائقة للمستخدم اليومي والقوة البرمجية للأداء العالي مع دعم كامل للغة العربية والوسائط المتعددة والأمان الحيوي.

---

## ✨ أبرز المزايا والخصائص (Key Features)

### ✍️ محرر نصوص متقدم وغني (Rich Editor)
- **حرية مطلقة في الكتابة:** كتابة نصوص غير محدودة الطول والحجم بدون أي تقييد.
- **تنسيق سريع ومرن:** دعم تنسيقات Markdown السريعة (عناوين H1-H3، عريض **Bold**، مائل *Italic*، قوائم نقطية ورقمية، قوائم مهام وقوائم اقتباس).
- **دعم كامل للغة العربية (BiDi & RTL):** توجيه ذكي للنصوص والمحاذاة يدعم اللغات من اليمين لليسار ومن اليسار لليمين بكل سلاسة.
- **مرفقات صور تفاعلية:** إمكانية إرفاق عدة صور لكل ملاحظة مع معرض مصغر وعارض صور يدعم التكبير والتصغير بالإيماءات (`Zoom & Pan`).
- **تخصيص الألوان:** إمكانية تغيير لون بطاقة الملاحظة بألوان مريحة للعين ومتناسقة مع الوضعين الفاتح والداكن.

### 🗂️ تنظيم وتصنيف مرن (Organization & Tags)
- **مجلدات مخصصة:** إنشاء وتعديل وحذف مجلدات لتصنيف الملاحظات بسهولة، مع إمكانية استخدام التطبيق بشكل مباشر وبسيط دون إجبار المستخدم على إنشاء مجلدات.
- **وسوم سريعة (#Tags):** تصنيف الملاحظات عبر الوسوم والوصول الفوري إليها عبر شريط التصفية السريع.
- **تثبيت الملاحظات الهامة (Pin):** تثبيت الملاحظات الأساسية في أعلى القائمة للوصول إليها بنقرة واحدة.
- **عرض شبكي وقائمي:** التبديل الفوري بين العرض الشبكي المتداخل (`Staggered Grid`) والعرض العمودي (`List View`).
- **تحديد متعدد (Batch Actions):** إمكانية تحديد عدة ملاحظات دفعة واحدة لحذفها أو نقلها لمجلد آخر.
- **بحث فوري وشامل:** محرك بحث محلي فائق السرعة يبحث في العناوين، النصوص، المجلدات، والوسوم.

### 🔒 الخصوصية والأمان العالي (Security & Privacy)
- **تشفير محلي:** حماية وتشفير الإعدادات والبيانات الحساسة.
- **قفل برمز PIN:** قفل التطبيق برمز PIN مخصص من 4 إلى 6 أرقام مع لوحة مفاتيح آمنة ومؤشرات تفاعلية.
- **دعم البصمة الحيوية (Biometric Authentication):** إمكانية فتح التطبيق ببصمة الإصبع أو الوجه عبر `androidx.biometric`.
- **ملاحظات مشفرة:** قفل وتشفير الملاحظات الحساسة بكلمة مرور.

### 💾 النسخ الاحتياطي والإحصائيات (Backup & Insights)
- **تصدير محلي آمن:** تصدير كافة الملاحظات والمجلدات إلى ملف `JSON` ومشاركته عبر التطبيقات.
- **استعادة البيانات:** استيراد النسخ الاحتياطية بنقرة واحدة من ذاكرة الجهاز.
- **شاشة الإحصائيات:** لوحة معلومات خفيفة تحسب إجمالي الملاحظات، الكلمات، الحروف، الصور، والمجلدات.

---

## 🏗️ البنية التقنية والمعمارية (Architecture & Tech Stack)

| المكون | التقنية المستخدمة |
| :--- | :--- |
| **لغة البرمجة** | Kotlin 2.2+ |
| **واجهة المستخدم** | Jetpack Compose + Material Design 3 |
| **المعمارية** | Clean Architecture + MVVM (Model-View-ViewModel) |
| **قاعدة البيانات المحلية** | Room Database (SQLite Engine) مع دعم الفهارس والمفاتيح الأجنبية |
| **تدفق البيانات والتزامن** | Kotlin Coroutines & StateFlow / SharedFlow |
| **معالجة الصور** | Coil 2.7+ مع إدارة الذاكرة والتخزين المؤقت |
| **الأمان والتشفير** | Jetpack Security Crypto + AndroidX Biometric API |
| **إدارة الحزم والبناء** | Gradle (Kotlin DSL) مع Version Catalogs (`libs.versions.toml`) |

---

## 📂 هيكل المشروع (Project Structure)

```text
app/src/main/java/com/example/
├── NoteApplication.kt          # Application class مع DI Container
├── data/
│   ├── local/                  # قاعدة بيانات Room (NoteDatabase, DAOs)
│   ├── model/                  # الكيانات (NoteEntity, FolderEntity)
│   ├── repository/             # المستودعات (NoteRepository, BackupRepository)
│   └── security/               # إدارة التشفير والـ PIN (SecurityManager)
├── ui/
│   ├── components/             # المكونات التفاعلية (NoteCard, Toolbar, Dialogs, PinPad)
│   ├── navigation/             # التنقل وإدارة المسارات (Screen Routes)
│   ├── screens/                # الشاشات الرئيسية (HomeScreen, Editor, Folders, Settings, Lock)
│   ├── theme/                  # السمات والألوان والخطوط (Material 3 Theme)
│   └── viewmodel/              # مزودي البيانات وحالة الواجهة (NotesViewModel, EditorViewModel)
```

---

## 🚀 كيفية البناء والتشغيل (How to Build & Run)

### 1. المتطلبات الأساسية
- **Android Studio Ladybug (أو أحدث)**
- **JDK 17 أو JDK 21**
- **Android SDK:** الحد الأدنى `API 24` (Android 7.0) والهدف `API 36`

### 2. البناء عبر سطر الأوامر (Gradle)
```bash
# استنساخ المشروع
git clone https://github.com/aymanpc11/NoteA.git
cd NoteA

# منح صلاحية التنفيذ لـ Gradle Wrapper
chmod +x gradlew

# بناء نسخة الـ APK بصيغة Debug
./gradlew assembleDebug

# ملف الـ APK الناتج سيكون في المسار التالي:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 البناء التلقائي عبر GitHub Actions (CI/CD)

المشروع مزود بملف عمل جاهز ومحدث وفق أعلى معايير GitHub Actions (`.github/workflows/build_apk.yml`):
- يتم البناء التلقائي عند كل `push` أو `pull_request` لفرع `main` أو `master`.
- يدعم التشغيل اليدوي (`workflow_dispatch`) لاختيار بناء نسخة **Debug** أو **Release**.
- يرفع ملف الـ **APK** كـ Artifact قابل للتنزيل فوراً من تبويب **Actions** في مستودع GitHub.

---

## 📄 الترخيص (License)
هذا المشروع مفتوح المصدر ومتاح بموجب ترخيص [MIT License](LICENSE).
