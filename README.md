# Japaco
Java Path Coverage Tools.

## 开发环境

- IDE: [Intellij IDEA](https://www.jetbrains.com/idea/) 

  （任意，但 Jetbrians 是真的牛逼）

- 依赖:

  - [ASM](https://asm.ow2.io/) `ver 7.2`
  - [Kotlin](https://kotlinlang.org/) `ver 1.3.41`
  - [JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) `ver 1.8.0_40`
  - [Gradle](https://gradle.org/) `ver 5.2.1`

  （除了 JDK 需要 1.8 以下，因为 ASM 的版本不支持 Java 8 以后的版本，其他要求应该不大，供参考）



## 文件结构

- /

  - 其他文件为 [Gradle](https://gradle.org/) 相关文件

    （在该项目中仅用于管理 Maven 库的依赖，自己手动加载 [ASM](https://asm.ow2.io/)、[Kotlin](https://kotlinlang.org/) 库亦可）

  - `src/` 为源码目录

    - `java/` 为待测试的 Java 代码

    - `kotlin/` 

      - `com.moi.japaco/` 项目源码

        - `config/` 常量文件

        - `data/` 一些 Java Bean 之类的数据文件，该项目没有封装，所以很空荡荡

        - `worker/` 一些工具人

          - **Analyzer** 通过字节码中获得的所有边，分析目标路径

          - **ClassDetector** 获取 Class Path 中需要测试的文件

          - **DataClassCreator** 负责生产存储静态变量的 Data.class

          - **Evaluator** 负责评价覆盖情况，保存覆盖结果

          - **PathClassAdapter** 字节码分析的适配器，在 ASM 过程中分析代码的边和插桩

            之后可以在这里添加获取 if 中变量的值（插桩），以及分析 if 的条件

          - **Reporter** 负责生成报表

        - **Japaco** 工具的入口，其他模块使用该类调用各种方法

      - `com.moi.sample/` 运行样例 

  - `build/` 为编译后的文件存放的目录，即 Java Class 文件放置地点

    这份源码中没有这个目录，在**编译后**才会生成

    ```kotlin
    val classPaths = "${System.getProperty("user.dir")}/build/classes/java/main/"
    ```

    样例代码中指定的 Class Path 就是指向这个目录

    **注意**：

    *如果你们 IDE 不同，或者配置不同，生成目录会发生改变，注意修改<br/>还有要注意的是，有些编译器采用了懒加载模式，二次编译时不会重新编译未经修改的代码<br/>故运行多次可能出现重复插桩情况，关闭懒加载或把 build 文件清除即可*

<br/>

## 大致过程

- Main

  - 设置待测 Class 文件目录、入口类、入口函数、待忽略的包名

  - 根据以上参数创建 Japaco 工具类

  - 调用 generate 方法，这一步可以写在 Japaco 的构造方法中，感觉必须是被调用的

  - 手动生成 Test Suites，调用 test 方法，得到覆盖情况

    *这一步做修改，通过覆盖情况计算 Fitness，用算法得到下一次的 Test Suites，重复直到满足停止条件*

  - 将覆盖情况生成报告

    

- Japaco

  - **generate()**

    整个算法执行的第一步，感觉没必要单独列出来，可以直接写在构造方法里

    过程：

    生成 Data.class

    在 Class Path 和 Ignore Package 中寻找需要分析的文件，进行分析

    将分析出来的边交给 Analyer 分析

  - **test(suites, classObj): Evaluator**

    输入：`suites` -> 所有测试用例；`classObj` -> 调用对象，测试非静态方法时，需要传递对象

    输出：`Evaluator` 这个工具人手上有覆盖情况，省事就直接把他返回了，当然这么做破坏了封装

    过程：

    加载待测试的类，对于每个测试用例，反射调用测试函数，将插桩的值保存

    将插桩的值和之前 Analyer 分析出来的内容交给 Evaluator，让它评价

    （同样这里直接传了 Analyzer 也是图方便，耦合了）

    注意：

    *不能在 generate 执行之前加载待测试的类，那样的话 class loader 所持有的测试类是未插桩状态的*

  - **report(...)** 这个没啥好说的



- 其他类

  - 我不想写了，看着办吧，不爽就推，思路永存

  - **Analyzer**

    得到了一大堆边，从入口函数的 START 开始，递归寻找调用了哪些函数（函数调用的节点在字节码获取边时特殊标记了），将函数的边添加到待测边中，替换这些函数调用（例如：A.L1->INVOKE B、 INVOKE B->A.L2 变成 A.L1->B.START、B.END->A.L2，这里要注意的是，INVOKE后面会接几个 Label，循环替换一下）直到运行到入口函数的 END，这就得到了所有可能经过的边。

    掐好 START 和 END，来一次图的 DFS，搜索到所有的路径，但这里的路径没有循环。DFS 的过程中如果遇到下个节点在栈中存在，表示遇到了循环，栈中存在的节点到栈顶的序列就是循环节。然后把循环节插入到之前搜索的所有路径中即可。

    该方法是基于简单路径覆盖的，所以插入的是单次循环节，做 K 次循环的话，就组合一下再插入。

    这就得到所有目标路径了，任务完成。

    

  - **ClassDetector**

    在所给 Class Path 中找没有被 ignore 的 class 文件，和它的路径名拼成包名，返回即可

    

  - **DataClassCreator**

    生成一个 Data.class 而已

    

  - **Evaluator**

    拿到 Test Suite 通过插桩输出的序列后直接和 Analyzer 算出来得目标路径匹配。但是因为有循环的存在，需要先处理一遍输出的序列。

    该方法认为一条路径走了循环里的多个分支，就覆盖了多个目标路径，所以就通过循环点，在序列里切出每个循环节，拼成多个单一循环的路径。

    如果做 K 次循环的话，也是找到循环点，只保留前 K 次的循环节，后面的抛弃即可。

    

  - **PathClassAdapter**

    这里主要是分析边和插桩的逻辑了。

    插桩：

    开始读代码的时候插入 START，遇到 RETURN 的时候插入 END（返回 void 其实也是 return）。<br/>每次读到 Label 的时候插入 Label（Label 只在跳转的目标位置出现）。<br/>if 跳转时，条件成立的目标会有个 Label，但条件不成立时不一定有，此时在条件不成立处插入 !Label以防万一。这样的后果是输出的图里面会有一些没用的分支节点，之后去除就好。

    分析边：

    保存一个当前存储的 Label，从 START 开始。<br/>遇到 if 类的跳转就添加"当前->跳转 Label"、"当前->!跳转 Label"两条边，然后用"!跳转 Label"替换当前 Label，为了之后的顺接。<br/>遇到 GOTO 这样的，添加"当前->跳转 Label"，并把当前 Label 置空，因为 GOTO 后面不能顺接了。<br/>遇到 SWITCH 这样的就相当于每个分支做了一个 GOTO。<br/>遇到函数调用，就添加"当前->函数 Label"，并用"函数 Label"替换当前 Label，为了之后的顺接。

    

  - **Reporter**

    这个没必要说了
