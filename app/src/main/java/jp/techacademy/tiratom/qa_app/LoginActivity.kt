package jp.techacademy.tiratom.qa_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.HashMap


class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference
    private lateinit var m_Database: FirebaseDatabase

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに登録する
    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference
        m_Database = FirebaseDatabase.getInstance()

        // FirebaseAuthのオブジェクトを取得
        mAuth = FirebaseAuth.getInstance()

        // アカウント作成処理のリスナー
        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功時、ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else {
                // 失敗した場合、エラーを表示する
                val view = findViewById<View>(R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()

                //プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }


        // ログイン処理時のリスナー
        mLoginListener = OnCompleteListener {task ->
            if (task.isSuccessful) {
                // ログイン成功時の処理
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)

                if (mIsCreateAccount) {
                    // アカウント作成の時は表示名をFirebaseに保存する
                    val name = nameText.text.toString()

                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)

                    // 表示名をPreferenceに保存する
                    saveName(name)

                } else {
                    userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String)
                        }

                        override fun onCancelled(p0: DatabaseError) {}

                    })
                }

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE

                // Activityを閉じる
                finish()

            } else {
                // 失敗した場合,エラーを表示する
                val view = findViewById<View>(R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE

            }
        }


        // UIの準備
        title = "ログイン"

        createButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            if (email.length != 0 && password.length >= 6 && name.length != 0) {
                // ログイン時に表示名を保存するようにフラグを立てる
                mIsCreateAccount = true

                createAccount(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        loginButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6) {
                // フラグを落としておく
                mIsCreateAccount = false

                login(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    override fun onStart() {
        super.onStart()

        val endAt: Double = Date().getTime().toDouble() // Dynamic value: NO CRASH
        getGoal("min_per_day", endAt, "some_uid")
    }

    private fun getGoal(p_goalId: String, p_endAt: Double, p_uid: String) {
        val ref = m_Database.getReference("v0/data/meditation/goals").child(p_goalId).child(p_uid)
            .orderByChild("time").endAt(p_endAt).limitToLast(1)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("FB", "Snapshot: $dataSnapshot")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FB", "Error: $error")
            }
        })
    }

    private fun createAccount(email: String, password: String)
    {
         // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)

    }


    private fun saveName(name: String) {
        // Preferenceに保存する
        val sp = getSharedPreferences(NameKEY, Context.MODE_PRIVATE)
        var editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }

    private fun login(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password)

    }
}
