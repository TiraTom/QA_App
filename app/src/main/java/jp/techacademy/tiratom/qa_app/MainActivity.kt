package jp.techacademy.tiratom.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {



    //TODO ログイン状態で開始ー＞ログアウトー＞お気に入りリストに移動　の時にException起きるのでそれを修正




    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            var map = dataSnapshot.value as Map<String, String>
            val questionUid = dataSnapshot.key!!

            // お気に入り選択時は、該当質問のデータを取得してくる
            if (mGenre == FavoriteGenre) {
                val targetQuestionRef = mDatabaseReference
                                        .child(ContentsPATH)
                                        .child(map[GenreKey] ?: "0")
                                        .child(dataSnapshot.key ?: error(""))

                    targetQuestionRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        map = dataSnapshot.value as Map<String, String>
                        setQuestionArrayData(map, questionUid)
                    }
                })

            } else {
                // ジャンル選択時はそのまま取得する
                map = dataSnapshot.value as Map<String, String>
                setQuestionArrayData(map, questionUid)
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更の可能性があるのは回答（Answer）のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            // お気に入り解除されたデータの削除
            mQuestionArrayList.toList().forEach { if (it.questionUid == dataSnapshot.key) mQuestionArrayList.remove(it) }

            mAdapter.notifyDataSetChanged()
        }
    }

    private fun setQuestionArrayData(map: Map<String, String>, questionUid: String = "") {
        val title = map["title"] ?: ""
        val body = map["title"] ?: ""
        val name = map["name"] ?: ""
        val uid = map["uid"] ?: ""
        val imageString = map["image"] ?: ""
        val bytes =
            if (imageString.isNotEmpty()) {
                Base64.decode(imageString, Base64.DEFAULT)
            } else {
                byteArrayOf()
            }

        val answerArrayList = ArrayList<Answer>()
        val answerMap = map["answers"] as Map<String, String>?
        if (answerMap != null) {
            for (key in answerMap.keys) {
                val temp = answerMap[key] as Map<String, String>
                val answerBody = temp["body"] ?: ""
                val answerName = temp["name"] ?: ""
                val answerUid = temp["uid"] ?: ""
                val answer = Answer(answerBody, answerName, answerUid, key)
                answerArrayList.add(answer)
            }
        }

        val question = Question(
            title, body, name, uid, questionUid,
            mGenre, bytes, answerArrayList)
        mQuestionArrayList.add(question)
        mAdapter.notifyDataSetChanged()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)

        // ログイン済みのユーザを取得
        val user = FirebaseAuth.getInstance().currentUser

        fab.setOnClickListener { view ->

            // ジャンル非選択時はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show()
            } else {

            }

            // ログインしていなければログイン画面に遷移
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        // ナビゲーションドロワーの設定
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        if (FirebaseAuth.getInstance().currentUser != null) {
            // ナビゲーションメニューに「お気に入り」を追加する
            navigationView.inflateMenu(R.menu.activity_main_drawer_favorite)
        }
        // 通常のナビゲーションメニューの設定
        navigationView.inflateMenu(R.menu.activity_main_drawer)

        navigationView.setNavigationItemSelectedListener(this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        if (FirebaseAuth.getInstance().currentUser != null) {
            // ナビゲーションメニューの設定。ログイン後なので「お気に入り」も追加する
            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.activity_main_drawer_favorite)
            navigationView.inflateMenu(R.menu.activity_main_drawer)

        } else {
            // 「お気に入り」を削除したナビゲーションメニューの設定を行う
            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.activity_main_drawer)
        }



        // 1:趣味を既定の選択とする
        if (mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_hobby -> {
                mToolbar.title = "趣味"
                mGenre = HobbyGenre
            }
            R.id.nav_life -> {
                mToolbar.title = "生活"
                mGenre = LifeGenre
            }
            R.id.nav_health -> {
                mToolbar.title = "健康"
                mGenre = HealthGenre
            }
            R.id.nav_computer -> {
                mToolbar.title = "コンピューター"
                mGenre = ComputerGenre
            }
            R.id.nav_favorite -> {
                mToolbar.title = "お気に入り"
                mGenre = FavoriteGenre
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }

        if (mGenre == FavoriteGenre) {
            // お気に入り選択時
            val currentUser = FirebaseAuth.getInstance().currentUser
            mGenreRef = mDatabaseReference.child(FavoritePATH).child(currentUser?.uid ?: "")
        } else {
            // ジャンル選択時
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        }

        mGenreRef!!.addChildEventListener(mEventListener)

        return true

    }
}
