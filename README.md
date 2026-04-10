# WCB Cafe POS System

WCB Cafe のための、シンプルでモダンな POS（販売時点管理）システムです。Java バックエンドとバニラ JavaScript フロントエンドで構築されており、カフェの運営を効率化します。

## 🚀 特徴

- **モダンなランディングページ**: ブランドイメージを重視した洗練されたデザイン。
- **高機能 POS レジ画面**: 商品カテゴリー選択、検索、カート管理、決済シミュレーション。
- **動的データ管理**: JSON ファイル（`data/`）による商品情報とニュースの集中管理。
- **注文履歴 & ダッシュボード**: 過去の注文の確認と、本日の売上統計の表示。
- **マルチデバイス対応**: レスポンシブな UI デザイン。

## 🛠 技術スタック

- **Backend**: Java (Standard JDK `HttpServer` API)
- **Frontend**: HTML5, CSS3 (CSS Variables), JavaScript (ES6+, Fetch API)
- **Data**: JSON ベースのファイルストレージ
- **Icons**: Font Awesome 6.5.1
- **Fonts**: Philosopher, Inter (Google Fonts)

## 📁 プロジェクト構造

```text
POS-SYSTEM/
├── data/               # システムデータ (JSON)
│   ├── products.json   # 商品マスタ
│   └── news.json       # お知らせデータ
├── src/                # Java ソースコード
│   └── PosServer.java  # HTTP サーバー & API ハンドラー
├── www/                # フロントエンド資産
│   ├── index.html      # ランディングページ
│   ├── news.html       # ニュース一覧
│   ├── contact.html    # お問い合わせ
│   ├── css/            # 共通スタイル
│   └── pos/            # POS システム画面
│       ├── index.html
│       ├── script.js
│       └── styles.css
└── README.md
```

## ⚡ 起動方法 (Tomcat)

### 前提条件
- Apache Tomcat 9.0 以上がインストールされていること。
- JDK 8 以上がインストールされていること。

### 手順

1. **コンパイル (Servlet)**:
   Tomcat の `lib` フォルダ内にある `servlet-api.jar` をクラスパスに含めてコンパイルします。
   ```powershell
   javac -cp "C:\path\to\tomcat\lib\servlet-api.jar" src\PosApiServlet.java -d WEB-INF\classes
   ```
   ※ `C:\path\to\tomcat` は実際の Tomcat インストールパスに置き換えてください。

2. **デプロイ**:
   プロジェクトのディレクトリ（`POS-SYSTEM`）ごと、Tomcat の `webapps` フォルダにコピーします。
   または、プロジェクトのルートで以下の内容を含む `war` ファイルを作成してデプロイします。

3. **アクセス**:
   Tomcat を起動し、ブラウザで以下の URL を開きます。
   `http://localhost:8080/POS-SYSTEM/index.html`

## ⚡ 起動方法 (簡易サーバー - 開発用)

1. **コンパイル**:
   ```powershell
   javac src/PosServer.java -d .
   ```

2. **サーバーの起動**:
   ```powershell
   java -cp . PosServer
   ```

3. **アクセス**:
   ブラウザで以下の URL を開きます。
   - ランディングページ: `http://localhost:8080/index.html`
   - POS システム: `http://localhost:8080/pos/index.html`

## 📧 お問い合わせ
- **Phone**: 80864355622
- **Email**: harrysoe2003@gmail.com

---
© 2026 Harrysoe. All rights reserved.
