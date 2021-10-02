# WorldRegenerator
資源ワールドなどの再生成を行い、それに伴う雑務を自動でやらせるプラグイン。

## 依存
ハード: MultiverseCore<br>
ソフト: WorldEdit, Chunky, Dynmap

## 動作
プラグインの起動時に各ワールドのスポーンポイントのチャンクに保存されている、以前の再生成日時とconfig.ymlのインターバルを確認して再生成を行うか決定する。<br>
まだ再生成を行わない場合は、次の再生成の日時をコンソールに表示する。再生成を行う場合は以下の処理を実行する。
* MultiverseCoreで再生成（シードは更新、その他は据え置きで、スポーンポイントだけは変更 or 続投をconfig.ymlで指定）
* WorldEditでベース建築を配置（config.ymlでschemを指定）
* Chunkyで全生成（config.ymlで詳細を指定）
* 全生成終了時にDynmapの/dynmap fullrender worldコマンドを実行

コマンド・権限はない。

** デフォルトで指定されているresourceやresouce_nether、resource_the_endなどは初回実行時にはもう再生成の対象となっているので注意！ **<br>
** その名前のワールドがすでにある場合は手動でconfigを配置し、中身を予め変更しておくこと！ **