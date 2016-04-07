## A lite android http library

如果你在开发一个应用或者某一个小模块，需要用到网络请求，可网络请求又不用那么频繁，而你又不想用一个太大的第三方库，比如`Volley` `Retrofit` 等。但是，又不想每次都封装一个网络请求库。那么你就可以用本项目。

它很小，只有一个文件，加上注释才300多行。Proguard后增加的体积基本可以忽略不计。假如你是一个视apk体积如生命的人，那么你完全不会想在一个网络请求很弱的项目中使用上面提到的lib。

当然，如果你有更好的想法，完全可以实现一个更好的，然后开源出来。方便大家，何乐不为。

所做的一切，完全都是为了让自己更`懒`。

生活不只眼前的苟且，还有。

## Features

- body
- Header
- 各种请求
- 上传文件
- 下载文件
- 请求重试
- 读取进度

## TODO
- 表单上传
- 同时上传多文件

## Examples

- 简单请求
```java
new LiteHttp.Request().setUrl(url).connect()
```
- 实现下载
```java
new LiteHttp.Request().setUrl(url)//
        .setStreamListener(new LiteHttp.StreamListener() {
            @Override
            public OutputStream getOutStream() {
                try {
                    file.createNewFile();
                    return new BufferedOutputStream(new FileOutputStream(file), 16 * 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            public InputStream getInStream() {
                return null;
            }
        })//
        .connect()
```


see more in module `sample`

## Download
Get this via

`Gradle`:
```groovy
compile 'me.yourbay.tools:liteHttp:1.0.0.1'
```

`Maven`:
```xml
<dependency>
  <groupId>me.yourbay.tools</groupId>
  <artifactId>liteHttp</artifactId>
  <version>1.0.0.1</version>
  <type>pom</type>
</dependency>
```

`Ivy`:
```xml
<dependency org='me.yourbay.tools' name='liteHttp' rev='1.0.0.1'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```


## License

    Copyright 2016 LiteHttp

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    
