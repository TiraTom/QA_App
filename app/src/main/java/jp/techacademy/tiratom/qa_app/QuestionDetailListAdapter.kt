package jp.techacademy.tiratom.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter(), ChildEventListener {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null
    private lateinit var favoriteButton: ImageButton

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val databaseReference = FirebaseDatabase.getInstance().reference
        val mFavoriteRef = databaseReference.child(FavoritePATH).child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(mQuestion.questionUid)

        mFavoriteRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

        })

    }

    override fun getItemViewType(position: Int): Int {

        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            val databaseReference = FirebaseDatabase.getInstance().reference
            val userUid = FirebaseAuth.getInstance().currentUser!!.uid
            val favoriteReference = databaseReference.child(FavoritePATH).child(userUid)
            favoriteReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseSnapshot: DatabaseError) {
                }

                override fun onDataChange(databaseSnapshot: DataSnapshot) {
                    val data = databaseSnapshot.value as Map<String, String>
                    val favoriteQuestions = data[userUid] as List<String>

                    if (favoriteQuestions.contains(mQuestion.questionUid)) {
                        favoriteButton.setImageResource(R.drawable.ic_star_black_24dp)
                    } else {
                        favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp)
                    }

                }
            })

            favoriteButton = convertView.findViewById<View>(R.id.favoriteIcon) as ImageButton

            // お気に入りボタンにお気に入り登録・解除機能を設定
            favoriteButton.setOnClickListener { v ->
                val favoriteButton = v!!.findViewById<View>(R.id.favoriteIcon) as ImageButton

                val databaseReference = FirebaseDatabase.getInstance().reference
                val userUid = FirebaseAuth.getInstance().currentUser!!.uid
                val favoriteReference = databaseReference.child(FavoritePATH).child(userUid)
                favoriteReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseSnapshot: DatabaseError) {
                    }

                    override fun onDataChange(databaseSnapshot: DataSnapshot) {
                        val data = databaseSnapshot.value as Map<String, String>
                        val favoriteQuestions = data[userUid] as List<String>

                        if (favoriteQuestions.contains(mQuestion.questionUid)) {
                            // TODO データ削除

                        } else {
                            // TODO データ追加
                            favoriteReference.push().setValue(mQuestion.questionUid)

                        }

                    }

                })

                favoriteReference.addChildEventListener(this)

            }


        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

        }

        return convertView
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    override fun onCancelled(dataSnapshot: DatabaseError) {
    }

    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        val favoriteQuestions = dataSnapshot.value as Map<String, String>

        if (FirebaseAuth.getInstance().currentUser == null) {
            favoriteButton.visibility = View.GONE
        } else {
            if (favoriteQuestions.containsValue(mQuestion.questionUid)) {
                favoriteButton.setImageResource(R.drawable.ic_star_black_24dp)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp)
            }
        }
    }

    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
        val favoriteQuestions = dataSnapshot.value as Map<String, String>

        if (favoriteQuestions.containsValue(mQuestion.questionUid)) {
            favoriteButton.setImageResource(R.drawable.ic_star_black_24dp)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp)
        }
    }

    override fun onChildRemoved(p0: DataSnapshot) {
    }


}