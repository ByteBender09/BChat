package com.example.bluetoothapp.classes

class DataManager private constructor() {
    private val userList: MutableList<User> = mutableListOf()

    companion object {
        @Volatile
        private var instance: DataManager? = null

        fun getInstance(): DataManager {
            return instance ?: synchronized(this) {
                instance ?: DataManager().also { instance = it }
            }
        }
    }

    fun addUser(user: User) {
        userList.add(user)
    }

    fun getUsers(): List<User> {
        return userList
    }

    fun addMessageToUser(userAddress: String, chat: Chat) {
        val user = userList.find { it.address == userAddress }
        user?.let {
            it.listMessage += chat
        }
    }

    fun getUserMessages(userAddress: String): Array<Chat> {
        val user = userList.find { it.address == userAddress }
        return user?.listMessage ?: emptyArray()
    }

    fun getUserByAddress(userAddress: String): User? {
        return userList.find { it.address == userAddress }
    }

    //Check isExist
    fun isExistUser(userAddress: String): Boolean {
        return userList.any { it.address == userAddress }
    }

    fun updateUserByAddress(userAddress: String, name: String, image: String) {
        val user = userList.find { it.address == userAddress }
        user?.let {
            it.name = name
            it.image = image
        }
    }

    fun updateNameByAddress(userAddress: String, name: String){
        val user = userList.find { it.address == userAddress }
        user?.let {
            it.name = name
        }
    }

    fun updateImageByAddress(userAddress: String, image: String){
        val user = userList.find { it.address == userAddress }
        user?.let {
            it.image = image
        }
    }

    fun isUserImageDefault(userAddress: String): Boolean {
        val user = userList.find { it.address == userAddress }
        return user?.image == "DEFAULT"
    }

    fun clear(){
        userList.clear()
    }
}
