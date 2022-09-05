
const val TODAY = "today"
const val TOMORROW = "tomorrow"
const val ANY_SCHEDULE = "*"
const val SCHEDULE_ELEMENT_SIZE = 3

private enum class Frequency {
    OnceDaily,
    OnceHourly,
    EveryMinuteOfAnHour,
    EveryMinute,
}

private data class Time(
    val hour: Int,
    val minute: Int,
): Comparable<Time> {
    override fun toString() = if(minute < 10) "$hour:0$minute" else "$hour:$minute"
    override fun compareTo(other: Time): Int = (hour - other.hour).let {
        when(it) {
            0 -> minute - other.minute
            else -> it
        }
    }
    companion object {
        fun parse(toParse: String): Time {
            val timeComponentList = toParse.split(":")
            return Time(
                hour = timeComponentList[0].toInt(),
                minute = timeComponentList[1].toInt(),
            )
        }
    }
    fun plusOneHour() = Time(
        hour = if(hour == 23) 0 else hour + 1,
        minute = minute,
    )
}

fun main(args: Array<String>) {
    try {
        processStdIn(Time.parse(args[0]))
    } catch (nfex: NumberFormatException) {
        usage()
    } catch (iooe: IndexOutOfBoundsException) {
        usage()
    } catch (rex: RuntimeException) {
        // NOOP. EOF
    }
}

/*
there is a choice to make. either
1) the function skips over a line without enough information and continues until the end of stdin
   and so it needs to be interrupted if stdin is typed instead of being piped
2) the function returns on an empty line
I chose the former as it seemed closer to the spirit of the exercise
*/

private fun processStdIn(now: Time) {
    var stdLine = readln()

    while (true) {
        val schedule = stdLine.split(" ", "\t", limit = 3)
        if(schedule.size >= SCHEDULE_ELEMENT_SIZE) {
            val minute = schedule[0]
            val hour = schedule[1]
            val command = schedule[2]

            try {
                val nextTime = when (frequencyFrom(hour = hour, minute = minute)) {
                    Frequency.EveryMinute -> now
                    Frequency.EveryMinuteOfAnHour -> findNextTimeEveryMinuteOfAnHour(hour.toInt(), now)
                    Frequency.OnceHourly -> findNextTimeOnceAnHour(minute.toInt(), now)
                    Frequency.OnceDaily -> Time(
                        hour = hour.toInt(),
                        minute = minute.toInt(),
                    )
                }
                val day = if (nextTime < now) TOMORROW else TODAY
                println("$nextTime $day - $command")
            } catch(nfex: NumberFormatException) {
                // NOOP. ignore malformed lines
            }
        }
        stdLine = readln()
    }
}

private fun frequencyFrom(hour: String, minute: String) = when (minute) {
    ANY_SCHEDULE -> when (hour) {
        ANY_SCHEDULE -> Frequency.EveryMinute
        else -> Frequency.EveryMinuteOfAnHour
    }
    else -> when (hour) {
        ANY_SCHEDULE -> Frequency.OnceHourly
        else -> Frequency.OnceDaily
    }
}

private fun findNextTimeOnceAnHour(minute: Int, now: Time) = Time(
    hour = if(minute > now.minute) now.hour else now.plusOneHour().hour,
    minute = minute,
)

private fun findNextTimeEveryMinuteOfAnHour(hour: Int, now: Time) =
    if(hour == now.hour) now
    else Time(
        hour = hour,
        minute = 0,
    )

private fun usage() {
    println("Input appears to be in unexpected format")
    println("argument should be (h)h:mm")
    println("stdin should be lines of: mm|* (h)h|* <command>")
}