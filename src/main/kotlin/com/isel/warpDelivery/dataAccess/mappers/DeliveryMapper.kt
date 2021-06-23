package com.isel.warpDelivery.dataAccess.mappers

import com.isel.warpDelivery.authentication.UserInfo
import com.isel.warpDelivery.dataAccess.DAO.Delivery
import com.isel.warpDelivery.dataAccess.DAO.StateTransition
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component

@Component
class DeliveryMapper(jdbi: Jdbi) : DataMapper<Int, Delivery>(jdbi) {

    companion object {
        const val DELIVERY_TABLE = "DELIVERY"
        const val TRANSITIONS_TABLE = "STATE_TRANSITIONS"
    }

    override fun create(DAO: Delivery) {
        jdbi.useTransaction<Exception> { handle ->

            handle.createUpdate(
                "Insert Into $DELIVERY_TABLE" +
                        "(deliveryid, state , purchasedate, deliverydate, rating, price, type) values" +
                        "(:id, :clientid, :warperid, :state, :purchasedate, :deliverydate, :rating, :price, :type)"
            )
                .bind("id", DAO.deliveryId)
                .bind("clientId", DAO.clientUsername)
                .bind("warperId", DAO.warperUsername)
                .bind("state", DAO.state)
                .bind("purchase_date", DAO.purchaseDate)
                .bind("delivery_date", DAO.deliveryDate)
                .bind("rating", DAO.rating)
                .bind("price", DAO.price)
                .bind("type", DAO.type)
                .execute()

            for (transition in DAO.transitions!!) {
                handle.createUpdate(
                    "Insert Into $TRANSITIONS_TABLE" +
                            "(deliveryId, transitiondate, previous_state, next_state) values" +
                            "(:id, :date, :prev_state, :next_state)"
                )
                    .bind("id", DAO.deliveryId)
                    .bind("date", transition.transitionDate)
                    .bind("prev_state", transition.previousState)
                    .bind("next_state", transition.nextState)
                    .execute()
            }

            handle.commit()
        }
    }

    override fun read(key: Int): Delivery =
        jdbi.inTransaction<Delivery, Exception> { handle ->
            val delivery = handle.createQuery(
                "SELECT deliveryid, clientid, warperid, " +
                        "state, purchasedate, deliverydate, rating, price, type " +
                        "from $DELIVERY_TABLE " +
                        "where deliveryid = :id"
            )
                .bind("id", key)
                .mapTo(Delivery::class.java)
                .one()

            val transitions = handle.createQuery(
                "SELECT  deliveryid, transitiondate, previousstate, nextstate " +
                        "from $TRANSITIONS_TABLE " +
                        "where deliveryid = :id"
            ).bind("id", key).mapTo(StateTransition::class.java).list()

            delivery.transitions = transitions

            return@inTransaction delivery
        }

    override fun update(DAO: Delivery) {
        jdbi.useTransaction<Exception> { handle ->
            handle.createUpdate(
                "update $DELIVERY_TABLE " +
                        "set state=:state, " +
                        "deliverydate=:deliverydate, " +
                        "rating=:rating" +
                        "where deliveryid=:deliveryid"
            )
                .bind("deliveryid", DAO.deliveryId)
                .bind("state", DAO.state)
                .bind("deliverydate", DAO.deliveryDate)
                .bind("rating", DAO.rating)
                .execute()
        }
    }

    override fun delete(key: Int) {
        jdbi.useTransaction<Exception> { handle ->
            handle.createUpdate(
                "DELETE from $DELIVERY_TABLE " +
                        "where deliveryid = :deliveryid"
            )
                .bind("deliveryid", key)
                .execute()
        }
    }

    fun readAll(): List<Delivery> =
        jdbi.inTransaction<List<Delivery>, Exception> { handle ->
            return@inTransaction handle.createQuery(
                "SELECT deliveryid,clientusername,warperusername,state " +
                        "from $DELIVERY_TABLE "
            )
                .mapTo(Delivery::class.java)
                .list()
        }

    fun updateState(key: String, state: String) =
        jdbi.useTransaction<Exception> { handle ->
            handle.createUpdate(
                "UPDATE $DELIVERY_TABLE " +
                        "SET state = :state" +
                        "where deliveryid = :deliveryid"
            )
                .bind("state", state)
                .bind("deliveryid", key)
                .execute()
        }

    fun getUserDeliveries(username: String): List<Delivery> =

        jdbi.inTransaction<List<Delivery>, Exception> { handle ->
            val deliveries = handle.createQuery("SELECT * FROM $DELIVERY_TABLE WHERE clientusername = :username")
                .bind("username", username)
                .mapTo(Delivery::class.java)
                .list()

            for (delivery in deliveries) {
                val transitions = handle.createQuery(
                    "SELECT  deliveryid, transitiondate, previousstate, nextstate " +
                            "from $TRANSITIONS_TABLE " +
                            "where deliveryid = :id"
                ).bind("id", delivery.deliveryId).mapTo(StateTransition::class.java).list()

                delivery.transitions = transitions
            }
            //TODO: Improve code

            return@inTransaction deliveries
        }

    fun getTransitions(deliveryId: Int): List<StateTransition> =
        jdbi.inTransaction<List<StateTransition> ,Exception> { handle ->

            return@inTransaction handle.createQuery(
                "SELECT transitiondate, previousstate, nextstate FROM $TRANSITIONS_TABLE " +
                        "WHERE deliveryid = :deliveryId"
            )
                .bind("deliveryId", deliveryId)
                .mapTo(StateTransition::class.java)
                .list()
        }

    fun verifyWarper(userInfo: UserInfo, deliveryId: Int): Boolean =
        jdbi.withHandle<String, Exception> { handle ->
            val deliveryExists = handle.createQuery("select count(*) FROM $DELIVERY_TABLE where deliveryid = :id")
                .bind("id", deliveryId)
                .mapTo(Int::class.java)
                .findFirst()
                .get() == 1

            if(!deliveryExists)
                throw NoSuchElementException("Delivery $deliveryId does not exist")

            handle.createQuery("select warperusername from $DELIVERY_TABLE where deliveryid = :id")
                .bind("id", deliveryId)
                .mapTo(String::class.java)
                .findFirst()
                .get()
        }.compareTo(userInfo.name) == 0

    fun verifyClient(userInfo: UserInfo, deliveryId: Int): Boolean =
        jdbi.withHandle<String, Exception> { handle ->
            val deliveryExists = handle.createQuery("select count(*) FROM $DELIVERY_TABLE where deliveryid = :id")
                .bind("id", deliveryId)
                .mapTo(Int::class.java)
                .findFirst()
                .get() == 1

            if(!deliveryExists)
                throw NoSuchElementException("Delivery $deliveryId does not exist")

            handle.createQuery("select clientusername from $DELIVERY_TABLE where deliveryid = :id")
                .bind("id", deliveryId)
                .mapTo(String::class.java)
                .findFirst()
                .get()
        }.compareTo(userInfo.name) == 0

    fun verifyIfWarperOrClient(userInfo: UserInfo, deliveryId: Int) : Boolean{
        return verifyClient(userInfo, deliveryId) || verifyWarper(userInfo, deliveryId);
    }
}

