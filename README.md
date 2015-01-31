### maven search plugin

`yum search ...`とか`apt-cache search ...`のような感じで
MavenCentralリポジトリからライブラリを探してbuild.sbtに追記するプラグイン。

##### 使い方

```sbt
mvnsrch {探してるライブラリのartifactIdとか}
```

空白区切りで検索条件を列挙できる。

検索結果が番号付きで20行だけ表示されるので
build.sbtに追加したいものを選ぶ。

追加したあとは`reload`してください。

##### 使用例

typesafe.configを追加する場合

```sbt
> mvnsrch typesafe config
```

と入力すると

```sbt
query: http://search.maven.org/solrsearch/select?rows=20&wt=xml&q="typesafe"+"config"
3 entry found
 0 : com.typesafe % config % 1.2.1
 1 : org.skife.com.typesafe.config % typesafe-config % 0.3.0
 2 : org.ocpsoft.rewrite % rewrite-config-typesafe % 2.0.0.Alpha5
```

のように出てくるので、「0」を入力すると

```build.sbt
libraryDependencies += "com.typesafe" % "config" % "1.2.1"
```

がbuild.sbtに追加されます。


##### セットアップ

plugins.sbtとかに以下内容を書く。

```
lazy val root = project.in(file(".")).dependsOn(
  uri("git://github.com/fkmt-disk/maven-search-plugin.git")
)

addSbtPlugin("orz.mvnsrch" % "maven-search-plugin" % "0.1")
```


##### その他

機能が機能なので、各sbtプロジェクトのBuild.scalaやplugins.sbtに書くのはちょっと違う気がする。
グローバルに入れるといいんじゃないかと思います。
