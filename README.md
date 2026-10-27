# TVOS Cards – بطاقات Overlay لصندوق أندرويد TV

تطبيق أندرويد يعرض بطاقات (الطقس، مواقيت الصلاة، المباريات، الرام، المعالج، الساعة) **فوق أي تطبيق** بالضغط المطوّل على مفتاح تختاره من الريموت (بلوتوث أو IR).

> ⚠️ المشروع لم يُبنَ على جهاز حقيقي قبل التسليم (لا يوجد Android SDK في بيئة الإنشاء). أول بناء على GitHub قد يحتاج تعديلاً بسيطاً — لو فشل أرسل لي سجل الأخطاء (Actions log).

## كيف يعمل
- **KeyService** (خدمة إتاحة Accessibility): تستمع لمفاتيح الريموت، وعند الضغط المطوّل على مفتاحك تُظهر/تُخفي الـ overlay. الـ overlay نفسه نافذة `TYPE_ACCESSIBILITY_OVERLAY` فيها WebView شفاف (لا تحتاج صلاحية "الظهور فوق التطبيقات").
- **app/src/main/assets/index.html**: واجهة البطاقات (نفس النافذة التفاعلية التي جرّبتها).
- **Bridge.kt / SysInfo.kt**: يقرأ الرام والمعالج والحرارة الحقيقية، ويغلق التطبيقات الخلفية (Clear all)، ويفتح الإعدادات والمساعد الصوتي.

## البناء على GitHub (بدون Android Studio)
1. أنشئ حساب على github.com ثم **New repository** (اسمه مثلاً `TVOSCards`، Public أو Private).
2. ارفع محتويات هذا المجلد بالكامل إلى المستودع. الأضمن استخدام git:
   ```bash
   cd TVOSCards
   git init
   git add .
   git commit -m "first commit"
   git branch -M main
   git remote add origin https://github.com/<اسمك>/TVOSCards.git
   git push -u origin main
   ```
   (أو GitHub Desktop. لو رفعت من المتصفح تأكد أن المجلد المخفي `.github/workflows` اترفع.)
3. افتح تبويب **Actions** في المستودع ← سيعمل workflow باسم **Build APK** تلقائياً (أو اضغط **Run workflow**).
4. بعد ~3–6 دقائق افتح التشغيل الناجح ← قسم **Artifacts** ← حمّل `TVOSCards-debug-apk` (zip فيه `app-debug.apk`).

## البناء محلياً (اختياري)
افتح المجلد في Android Studio ثم Build ▸ Build APK. أو من الطرفية (Gradle 8.9 + JDK 17): `gradle assembleDebug`.

## التثبيت على الصندوق
```bash
adb connect <IP الصندوق>:5555
adb install -r app-debug.apk
```
(أو انسخ الـ APK على USB وثبّته بمدير ملفات.)

## التفعيل (مرة واحدة)
1. افتح التطبيق **TVOS Cards** ← **1. Enable the service** ← فعّل *TVOS Cards overlay key*.
   - أندرويد 13+: لو الزر رمادي: إعدادات ▸ التطبيقات ▸ TVOS Cards ▸ ⋮ ▸ **Allow restricted settings**، ثم أعد المحاولة.
   - أو عبر adb:
     ```bash
     adb shell settings put secure enabled_accessibility_services com.example.tvoscards/com.example.tvoscards.KeyService
     adb shell settings put secure accessibility_enabled 1
     ```
     (هذا يستبدل أي خدمات إتاحة مفعّلة سابقاً.)
2. (اختياري لقائمة التطبيقات) **2. Allow usage access** أو: `adb shell appops set com.example.tvoscards GET_USAGE_STATS allow`
3. **3. Record the toggle key** ثم اضغط المفتاح المطلوب من الريموت. (يمكنك أيضاً تسجيله من داخل الـ overlay: الإعدادات ▸ OVERLAY TOGGLE KEY ▸ Record key.)

## الاستخدام
- **ضغط مطوّل** على مفتاحك = إظهار/إخفاء البطاقات فوق أي تطبيق.
- الأسهم تنقّل بين البطاقات، OK يفتح، OK مطوّلاً على بطاقة = تحريكها، Back يغلق النافذة ثم الـ overlay.
- إشعارات الصلاة والمباريات تظهر كبطاقة خفيفة لـ 9 ثوانٍ حتى لو الـ overlay مخفي.

## حدود يجب معرفتها
- **مفتاح HOME**: أندرويد لا يسمح للتطبيقات باعتراضه. استخدم MENU أو أي مفتاح آخر (الأحمر/الأخضر/Bookmark…). إن لم يصل المفتاح للتطبيق فلن يُسجَّل.
- **حرارة/استخدام المعالج**: تُقرأ من `/sys/class/thermal/*` و`/proc/stat`. بعض الأجهزة تمنع `/proc/stat` (أندرويد 8+)؛ عندها يُقدَّر الحمل من تردد المعالج ويظهر مصدر القراءة في نافذة CPU. إن لم توجد حرارة يظهر N/A. (Goke 6323 وAmlogic لم أستطع تجربتها فعلياً.)
- **الرام لكل تطبيق**: أندرويد الحديث يخفي استهلاك التطبيقات الأخرى، لذا قد يظهر "—". زر Clear all يغلق التطبيقات الخلفية فقط ولا يغلق التطبيق الظاهر.
- **الطقس والمباريات**: لا تزال بيانات تجريبية داخل الواجهة (طقس لـ 7 مدن + نسخة ثابتة من كووورة). الربط الحي (Open-Meteo وكووورة) يحتاج خطوة إضافية. مواقيت الصلاة تُحسب محلياً.
- نسخة WebView القديمة على بعض الصناديق قد تُظهر الشكل أبسط (بدون blur).
