package jp.techacademy.tiratom.qa_app

const val UsersPATH = "users"       // Firebaseにユーザの表示名を保存するパス
const val ContentsPATH = "contents" // Firebaseに質問を保存するバス
const val AnswersPATH = "answers"   // Firebaseに解答を保存するパス
const val NameKEY = "name"          // Preferenceに表示名を保存する時のキー
const val FavoritePATH = "favorite" // Firebaseにユーザごとのお気に入りの質問を保存するパス
const val GenreKey = "genre"        // Firebaseにお気に入りの質問を登録する際に一緒に登録するGenreのキー

const val PreferenceKEY = "QAAppPreference"  // SharedPreferenceのファイル名

const val HobbyGenre = 1            // Navigationバーの「趣味」選択時の値
const val LifeGenre = 2             // Navigationバーの「生活」選択時の値
const val HealthGenre = 3           // Navigationバーの「健康」選択時の値
const val ComputerGenre = 4         // Navigationバーの「コンピューター」選択時の値
const val FavoriteGenre = 5         // Navigationバーの「お気に入り」選択時の値