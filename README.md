# CodeRemover
Процессор аннотаций. Удаляет помеченные классы и методы. Мы используем его для сборки клиентских и серверных (полных) версий модификаций. Данный процессор скорее является дополнением к трюку с компилятором Java, удаляющим код, который не выполнится никогда.

**Что умеет?**
* Удаляет классы, подклассы, методы и поля.
* При удалении класса-родителя все классы, наследующие удалённый будут перенаправлены на родительский класс удалённого (связывание разорванной цепочки наследования).
* В случае удаления интерфейса его использование (implements) будет удалено из всех классов-реализаций в обрабатываемом архиве. Сделано это, чтобы не получить ошибку об отсутствии класса.
* При удалении поля выполняется чистка конструкторов класса, дабы не получить ошибку о присвоении значения несуществующему классу. Работа данной функции пока оставляет желать лучшего.

Руководство пользователя смотрите в "Wiki".
