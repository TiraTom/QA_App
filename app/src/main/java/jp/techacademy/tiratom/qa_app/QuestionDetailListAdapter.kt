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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

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
        val mFavoriteRef = databaseReference.child(FavoritePATH).child(FirebaseAuth.getInstance().currentUser!!.uid).child(mQuestion.questionUid)

        mFavoriteRef.addChildEventListener(object: ChildEventListener {
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

            favoriteButton = convertView.findViewById<View>(R.id.favoriteIcon) as ImageButton

            favoriteButton.setOnClickListener{v ->
                val favoriteButton = v!!.findViewById<View>(R.id.favoriteIcon) as ImageButton

                val databaseReference = FirebaseDatabase.getInstance().reference
                val userUid = FirebaseAuth.getInstance().currentUser!!.uid
                val favoriteReference = databaseReference.child(FavoritePATH).child(userUid)
                favoriteReference.addChildEventListener(this)

                favoriteReference.push().setValue(mQuestion.questionUid)
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