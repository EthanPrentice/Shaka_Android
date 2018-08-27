package ethanprentice.com.partyplaylist.adt

/**
 * Created by Ethan on 2018-06-17.
 */
data class ErrorMessage(
        val message : String,
        val detailedMessage : String?,
        val code : Int
)