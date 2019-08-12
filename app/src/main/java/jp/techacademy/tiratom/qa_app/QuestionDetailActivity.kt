package jp.techacademy.tiratom.qa_app

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_detail.fab
import kotlinx.android.synthetic.main.app_bar_main.*
import android.R.menu
import android.view.MenuInflater
import android.widget.ImageButton


class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    private var isFavoriteQuestion: Boolean = false

    private val mEventListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()

        }

        override fun onChildRemoved(p0: DataSnapshot) {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_question_detail)

        // Toolbarの設定
        setSupportActionBar(findViewById(R.id.questionDetailToolbar))

        //　渡ってきたQuestionオブジェクトを保持
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val currentUser = FirebaseAuth.getInstance().currentUser

        fab.setOnClickListener {
            // ログイン済みのユーザを取得する
            if (currentUser == null) {
                // ログインしていなければログイン画面に遷移
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef =
            databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid)
                .child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        mFavoriteRef = databaseReference.child(FavoritePATH).child(currentUser!!.uid)
        mFavoriteRef.addChildEventListener(mFavoriteEventListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_question_detail_favorite, menu)

        val currentUser = FirebaseAuth.getInstance().currentUser

        val favoriteIcon = menu!!.findItem(R.id.favoriteIcon)
        if (currentUser?.uid != null) {
            // ログインしているのでお気に入りアイコンを表示する
            favoriteIcon.isVisible = true

            //　お気に入りの質問かどうかのチェック
            if (isFavoriteQuestion) {
                favoriteIcon.setIcon(R.drawable.ic_star_black_24dp)
            } else {
                favoriteIcon.setIcon(R.drawable.ic_star_border_black_24dp)
            }
        } else {
            // ログインしていないのでお気に入り表示はしない
            favoriteIcon.isVisible = false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favoriteIcon -> {

                if (isFavoriteQuestion) {
                    // お気に入りの取り消しを行う
                    // TODO 消せてなさそう
                    mFavoriteRef.child(mQuestion.questionUid).removeValue()

                    isFavoriteQuestion = false
                    item.setIcon(R.drawable.ic_star_border_black_24dp)

                } else {
                    // お気に入り登録を行う
                    val insertData = HashMap<String, String>()
                    insertData.put(GenreKey, mQuestion.genre.toString())
                    mFavoriteRef.child(mQuestion.questionUid).setValue(insertData)

                    isFavoriteQuestion = true
                    item.setIcon(R.drawable.ic_star_black_24dp)
                }
            }
        }
        return true
    }

    private val mFavoriteEventListener = object : ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
        }

        override fun onChildMoved(databaseSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildChanged(databaseSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildAdded(databaseSnapshot: DataSnapshot, s: String?) {
            val data = databaseSnapshot.value as Map<String, String>
            // Firebaseに今の表示している質問がお気に入り登録されていれば、お気に入りフラグを立てる
            isFavoriteQuestion = data != null
        }

        override fun onChildRemoved(databaseSnapshot: DataSnapshot) {
        }


    }
}

