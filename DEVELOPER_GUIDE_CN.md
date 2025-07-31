
# FoodRemind 应用开发指南 (中文版)

欢迎来到 FoodRemind 的开发世界！本文档旨在帮助初学者，特别是刚接触 Jetpack Compose 的朋友们，理解这个应用是如何从零到一构建起来的。我们会用尽可能简单的语言，剖析代码背后的思想。

---

## 1. 应用概述

FoodRemind 是一款专为提醒用户按时吃饭而设计的安卓应用。它不仅提供定时的三餐提醒，还有一个“食物选择器”来解决“今天吃什么”的世纪难题，并能记录用户的饮食历史。

**核心技术栈:**
*   **编程语言:** Kotlin
*   **UI 框架:** Jetpack Compose (这是我们学习的重点)
*   **架构模式:** MVVM (Model-View-ViewModel)
*   **异步处理:** Kotlin Flow

---

## 2. 项目结构导览

首先，我们来看看项目的“地图”，了解不同文件都放在哪里，各自负责什么。

```
FoodRemind/
└── app/
    └── src/
        └── main/
            ├── AndroidManifest.xml      # 应用的“户口本”，定义权限、组件等
            ├── java/com/jmin/foodremind/
            │   ├── data/                # 数据层 (负责数据的获取和存储)
            │   │   ├── model/           # 数据模型 (定义数据的结构)
            │   │   └── repository/      # 仓库 (管理数据来源)
            │   ├── provider/            # 后台服务提供者 (如闹钟)
            │   ├── ui/                  # UI界面层 (所有可见的界面都在这)
            │   │   ├── components/      # 可复用的UI组件
            │   │   ├── navigation/      # 导航逻辑
            │   │   ├── screens/         # 完整的屏幕界面
            │   │   ├── theme/           # 主题 (颜色、字体、样式)
            │   │   └── viewmodel/       # ViewModel (连接UI和数据)
            │   ├── MainActivity.kt      # 应用主入口
            │   └── ReminderActivity.kt  # 全屏提醒的界面
            └── res/                     # 资源文件夹
                ├── values/              # 存放字符串、颜色等资源
                └── values-zh-rCN/       # 针对简体中文的资源 (多语言支持)
```

**简单理解:**
*   **`ui` 包:** 是我们关注的重点，所有 Compose 代码都在这里。
*   **`data` 包:** 负责提供数据，比如从手机存储中读取用户的设置。
*   **`viewmodel` 包:** 是 UI 和数据之间的“大脑”和“协调者”。

---

## 3. Jetpack Compose 核心概念实战

Jetpack Compose 是一个现代的 UI 工具包，它的核心思想是：**你只需要描述你想要的 UI 是什么样子的，剩下的交给系统来处理。**

### 3.1. 万物皆可“组合” (Composable)

在 Compose 中，UI 元素都是由带有 `@Composable` 注解的函数创建的。我们称这些函数为 **可组合项 (Composable)**。

**示例：一个自定义按钮**

来看看 `app/src/main/java/com/jmin/foodremind/ui/components/CommonComponents.kt` 文件中的 `PrimaryButton`：

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit, // 点击事件是一个函数
    // ... 其他参数
) {
    Button(
        onClick = onClick, // 把接收到的点击事件传给真正的按钮
        // ... 样式设置
    ) {
        if (icon != null) {
            Icon(...)
        }
        Text(text = text) // 显示传入的文字
    }
}
```

*   `@Composable` 告诉编译器这是一个 UI 组件。
*   这个函数像一个模板，接收 `text` (文字)、`icon` (图标) 和 `onClick` (点击时做什么) 作为参数。
*   这样，在应用里的任何地方，我们想创建一个主要按钮时，只需要调用 `PrimaryButton(...)` 即可，而不用每次都重复写 `Button`、`Icon`、`Text` 和一堆样式代码。这就是 **复用**。

### 3.2. 搭建界面：屏幕 (Screen) 与布局

一个完整的屏幕通常由多个小的 Composable 组合而成。

**示例：主屏幕 (`HomeScreen.kt`)**

主屏幕由几个部分构成：顶部的问候语、中间的倒计时、底部的两个功能按钮。在代码中，这是通过布局组件 `Column` (垂直排列) 和 `Row` (水平排列) 来实现的。

```kotlin
// in: app/src/main/java/com/jmin/foodremind/ui/screens/HomeScreen.kt

@Composable
fun HomeScreen(
    // ... 参数
) {
    Column( // 所有东西从上到下垂直排列
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // 1. 顶部的问候语部分
        GreetingSection(...)

        // 2. 中间的倒计时部分
        CountdownSection(...)

        // 3. 底部的操作按钮部分
        ActionButtons(...)
    }
}
```
*   `Column` 像一个垂直的容器，把里面的东西一个个叠起来。
*   `Modifier` 是 Compose 中非常强大的工具，用来给组件添加各种“属性”，比如大小 (`fillMaxSize`)、边距 (`padding`)、背景颜色、点击事件等。你可以把它想象成给组件“化妆”和“塑形”的工具。

### 3.3. 让界面“动”起来：状态 (State)

静态的界面是无趣的。当用户输入文字、点击按钮时，界面需要响应变化。在 Compose 中，这种变化是由 **状态 (State)** 驱动的。

**核心思想：UI = f(State)**

这意味着你的 UI 始终是你的应用状态的一个函数。当状态改变时，UI 会自动“重组”（Recompose）来反映新的状态，你不需要手动去更新界面。

**示例：记录吃饭的对话框**

在 `DialogComponents.kt` 的 `RecordMealDialog` 中，用户需要输入食物名称。这个输入的文本就是一个状态。

```kotlin
// in: app/src/main/java/com/jmin/foodremind/ui/components/DialogComponents.kt
@Composable
fun RecordMealDialog(...) {
    var foodName by remember { mutableStateOf("") } // 1. 定义一个状态来存储食物名称
    // ...
    OutlinedTextField(
        value = foodName, // 2. 输入框显示这个状态的值
        onValueChange = { foodName = it }, // 3. 当用户输入时，更新这个状态
        label = { Text("吃了什么？") }
    )
    // ...
}
```
*   `mutableStateOf("")` 创建了一个可变的状态，初始值是空字符串。
*   `remember` 是一个关键的函数，它告诉 Compose “记住”这个状态。如果没有它，每次界面刷新（重组），`foodName` 都会被重置为空字符串。
*   当用户在输入框里打字时，`onValueChange` 被触发，它用新的文本更新 `foodName` 状态。
*   Compose 检测到 `foodName` 状态变化后，会自动重绘（重组）`OutlinedTextField`，让输入框显示出最新的内容。

### 3.4. 大脑与协调者：ViewModel

如果所有状态都放在 Composable 函数里，代码会变得混乱，而且当屏幕旋转时，这些“记住”的状态会丢失。因此，我们引入了 `ViewModel`。

**ViewModel 的职责：**
1.  **持有和管理 UI 状态**：它比 Composable 活得更久，所以不怕屏幕旋转。
2.  **处理业务逻辑**：当用户点击按钮时，Composable 会通知 ViewModel，由 ViewModel 来执行具体的操作（比如保存数据）。

**示例：主屏幕的倒计时**

主屏幕的倒计时 "距离下一餐还有 hh:mm:ss" 是一个不断变化的状态。这个状态就由 `FoodViewModel.kt` 管理。

```kotlin
// in: app/src/main/java/com/jmin/foodremind/ui/viewmodel/FoodViewModel.kt
class FoodViewModel(...) : ViewModel() {
    // 1. 使用 StateFlow 来持有倒计时状态
    private val _nextMealCountdown = MutableStateFlow("00:00:00")
    val nextMealCountdown: StateFlow<String> = _nextMealCountdown.asStateFlow()

    init {
        // 2. 启动一个定时器，每秒更新倒计时状态
        viewModelScope.launch {
            while (isActive) {
                _nextMealCountdown.value = calculateCountdown() // 更新状态
                delay(1000) // 等待1秒
            }
        }
    }
}

// in: app/src/main/java/com/jmin/foodremind/ui/screens/HomeScreen.kt
@Composable
fun HomeScreen(viewModel: FoodViewModel, ...) {
    // 3. 在 UI 中监听 ViewModel 的状态
    val countdownText by viewModel.nextMealCountdown.collectAsState()

    // 4. 直接使用这个状态
    Text(
        text = countdownText,
        style = MaterialTheme.typography.headlineLarge
    )
}
```

**这个流程被称为“单向数据流” (Unidirectional Data Flow):**
1.  **事件 (Event) 上行**：UI（如按钮点击）通知 ViewModel。
2.  **状态 (State) 下行**：ViewModel 更新状态，UI 监听状态变化并自动刷新。

这让应用的逻辑变得非常清晰和可预测。

### 3.5. 在屏幕间跳转：导航 (Navigation)

应用有多个屏幕（主页、设置、历史记录等），我们需要一种方式在它们之间切换。这由 `NavHost` 和 `NavController` 完成。

`app/src/main/java/com/jmin/foodremind/ui/navigation/MainNavigation.kt` 是我们应用的导航中心。

```kotlin
@Composable
fun MainNavigation(viewModel: FoodViewModel) {
    val navController = rememberNavController() // 创建一个导航控制器

    Scaffold(
        topBar = { /* ... 顶栏 ... */ }
    ) { paddingValues ->
        NavHost( // 导航容器，像一个“坑”
            navController = navController,
            startDestination = "home", // 指定起始页
            modifier = Modifier.padding(paddingValues)
        ) {
            // 定义每一个屏幕
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onFoodPickerClick = { navController.navigate("food_picker") } // 点击时导航到食物选择器
                )
            }
            composable("food_picker") {
                FoodPickerScreen(viewModel = viewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            // ... 其他屏幕
        }
    }
}
```

*   `NavHost` 定义了一个导航区域。
*   `composable("route_name") { ... }` 为每个屏幕定义了一个“路由地址”。
*   `navController.navigate("route_name")` 就是命令导航控制器“跳转到”指定地址的屏幕。

---

## 4. 特色功能解析

### 4.1. 多语言支持

应用的文字如何变成中文、英文、日文？
这依赖于 Android 的资源系统。

*   `res/values/strings.xml` 存放默认（英文）的字符串。
*   `res/values-zh-rCN/strings.xml` 存放简体中文的字符串。
*   `res/values-ja-rJP/strings.xml` 存放日文的字符串。

所有这些文件里都有相同名字的字符串，但内容是不同的语言。

**在 Compose 中使用:**

```kotlin
// 不好的做法 (硬编码):
Text(text = "Food Picker")

// 好的做法 (使用资源):
Text(text = stringResource(id = R.string.food_picker_btn))
```
`stringResource()` 函数会根据系统当前的语言设置，自动去对应的 `strings.xml` 文件里查找并显示文字。

动态切换语言是通过 `LocaleManager.kt` 实现的，它会更改应用的语言配置，并重启 `MainActivity` 来让新语言生效。

### 4.2. 后台闹钟与全屏提醒

即使用户关闭了应用，也要能准时提醒，这是如何做到的？

1.  **设置闹钟**：当用户在设置页设定了提醒时间后，`FoodViewModel` 会调用 `AlarmScheduler`。`AlarmScheduler` 使用 Android 系统的 `AlarmManager` 来注册一个未来的、精确的闹钟。这就像告诉系统：“嘿，在早上8点的时候叫醒我”。

2.  **接收闹钟**：到了指定时间，系统会发送一个广播。我们应用里的 `AlarmReceiver` (`BroadcastReceiver`) 会接收到这个广播。

3.  **弹出提醒**：`AlarmReceiver` 收到广播后，会立刻启动一个特殊的 Activity - `ReminderActivity.kt`。这个 Activity 被设计成透明的、覆盖全屏的样式，并播放震动和声音，从而实现了强提醒的效果。

---

## 5. 总结

希望这份文档能帮助你理解 FoodRemind 应用的运作方式。回顾一下最重要的几点：

*   **万物皆可组合**：UI 是由一个个 `@Composable` 函数嵌套而成的。
*   **UI = f(State)**：UI 是状态的直接反映。改变状态，UI 自动更新。
*   **单向数据流**：事件从 UI 到 ViewModel，状态从 ViewModel 到 UI，清晰可控。
*   **善用预览**：在你的 Composable 函数上添加 `@Preview` 注解，就可以在 Android Studio 中直接预览 UI 效果，无需每次都运行整个应用。

从这里开始，你可以尝试修改一些简单的东西，比如：
*   在 `HomeScreen.kt` 里改变一下文字的颜色或大小。
*   在 `PrimaryButton` 里修改它的圆角大小。
*   尝试给 `SettingsScreen.kt` 添加一个新的设置项。

祝你在 Jetpack Compose 的世界里探索愉快！ 