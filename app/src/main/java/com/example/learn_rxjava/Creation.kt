package com.example.learn_rxjava

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class Creation {
    fun exec() {
        Consumer(Producer()).exec()
    }

    class Producer {
        fun just(): Observable<String> {
            return Observable.just("1", "2", "3")
        }

        fun fromIterable(): Observable<String> {
            return Observable.fromIterable(listOf("1", "2", "3"))
        }

        fun interval() =
            Observable.interval(1, TimeUnit.SECONDS) //бесконечно передает числа с 0 рраз в сек

        fun timer() =
            Observable.timer(1, TimeUnit.SECONDS) // передает число единожды через 1 сек(к примеру)

        fun range() = Observable.range(1, 10) // передает числа от 1 до 10


        //имитация получения данных из вне
        fun randomResultOperation(): Boolean {
            Thread.sleep(Random.nextLong(1000))
            return listOf(true, false, true)[Random.nextInt(2)]
        }


        fun fromCallable() = Observable.fromCallable {
            //Симуляция долгих вычислений
            val result = randomResultOperation()
            return@fromCallable result
        }


        fun create() = Observable.create<String> { emitter ->
            try {
                for (i in 0..10) {
                    randomResultOperation().let {
                        if (it) {
                            emitter.onNext("Success$i")
                        } else {
                            emitter.onError(RuntimeException("Error"))
                            return@create
                        }
                    }
                }
                emitter.onComplete()
            } catch (t: Throwable) {
                emitter.onError(RuntimeException("Error"))
            }
        }


    }

    class Consumer(val producer: Producer) {
        val stringObserver = object : Observer<String> {
            var disposable: Disposable? = null
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                disposable = d
                println("onSubscribe")
            }

            override fun onNext(s: String) {
                println("onNext: $s")
            }

            override fun onError(e: Throwable) {
                println("onError: ${e?.message}")
            }
        }

        fun exec() {
            execJust()
        }

        fun execJust() {
            producer.just()
                .subscribe(stringObserver)
        }

        fun execFromIterable() {
            producer.fromIterable()
                .subscribe(stringObserver)
        }

        fun execInterval() {
            producer.interval()
                .subscribe {
                    println("onNext: $it")
                }
        }


        fun execTimer() {
            producer.timer()
                .subscribe {
                    println("onNext: $it")
                }
        }

        fun execRange() {
            producer.range()
                .subscribe {
                    println("onNext: $it")
                }
        }

        fun execCallable() {
            producer.fromCallable()
                .subscribe {
                    println("onNext: $it")
                }
        }


    }
}

class Operators {
    fun exec() {
        Consumer(Producer()).exec()
    }

    class Producer {
        fun createJust() = Observable.just("1", "2", "3", "3")
        fun createJust2() = Observable.just("4", "5", "6")

    }

    class Consumer(val producer: Producer) {
        fun exec() {
        }

        //создает надстройку над первым наблюдателем, в нашем случае выбирая первые цифры
        fun execTake() {
            producer.createJust()
                .take(2)
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        //создает надстройку над первым наблюдателем, в нашем случае пропуская первые цифры
        fun execSkip() {
            producer.createJust()
                .skip(2)
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        //один из основных операторов, позволяющий выполнять любые действия с данными внутри map
        fun execMap() {
            producer.createJust()
                .map { it + it }
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        // удаляет дубликаты
        fun execDistinct() {
            producer.createJust()
                .distinct()
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        //фильтрация данных
        fun execFilter() {
            producer.createJust()
                .filter() { it.toInt() > 1 }
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        // обьединяет источники данных
        fun execMerge() {
            producer.createJust()
                .mergeWith(producer.createJust2())
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        //похожа на map, но возвращает observable
        fun execFlatMap() {
            producer.createJust()
                .flatMap {
                    val delay = Random.nextInt(1000).toLong()
                    return@flatMap Observable.just(it + "x").delay(
                        delay,
                        TimeUnit.MILLISECONDS
                    )
                }
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }

        //позволяет подписаться на несколько источников данных одновременно и следить за ними.
        fun execZip() {
            val observable1 = Observable.just("1").delay(1, TimeUnit.SECONDS)
            val observable2 = Observable.just("2").delay(2, TimeUnit.SECONDS)
            val observable3 = Observable.just("3").delay(4, TimeUnit.SECONDS)
            val observable4 = Observable.just("4").delay(6, TimeUnit.SECONDS)



            Observable.zip(observable1, observable2, observable3, observable4,
                object : Function4<String, String, String, String, List<String>> {
                    override fun invoke(
                        t1: String,
                        t2: String,
                        t3: String,
                        t4: String
                    ): List<String> {
                        return listOf(t1, t2, t3, t4)
                    }
                })
                .subscribeOn(Schedulers.computation())
                .subscribe({
                    println("Zip result: $it")
                }, {
                    println("onError: ${it.message}")
                })
        }




        //ДЗ #2
        //Переделать ф-ю из flatmap в switchMap
        //отличается от flatMap тем, что позволяет отписываться от прошлого источника данных, если новый источник данных
        //например, можно использовать при создании метода для поиска. switchMap будет отменять предыдуший запрос при наборе
        // нового символа и в итоге отправит полный запрос, сэкономив трафик и вычислительную мощность сервера :)
        fun execSwitchMap() {
            producer.createJust()
                .switchMap {
                    val delay = Random.nextInt(1000).toLong()
                    return@switchMap Observable.just(it + "x").delay(
                        delay,
                        TimeUnit.MILLISECONDS
                    )
                }
                .subscribe({ s ->
                    println("onNext: $s")
                }, {
                    println("onError: ${it.message}")
                })
        }
    }
}


