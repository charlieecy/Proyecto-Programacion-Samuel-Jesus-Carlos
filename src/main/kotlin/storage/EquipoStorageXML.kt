package org.example.storage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import org.example.dto.EquipoDTO
import org.example.dto.IntegranteXmlDTO
import org.example.exceptions.Exceptions
import org.example.mapper.toModel
import org.example.mapper.toXmlDTO
import org.example.models.Entrenador
import org.example.models.Integrante
import org.example.models.Jugador
import org.lighthousegames.logging.logging
import java.io.File

class EquipoStorageXML:EquipoStorage {
    private var logger = logging()

    /**
     * Permite leer de un archivo una lista de [Integrante]
     * Lee el archivo como una lista de DTO de integrante y lo mapea al modelo segun va leyendo
     * @throws [Exceptions.StorageException] Si el fichero no existe, no es un fichero o no se tienen permisos de lectura
     * @return Lista de integrantes
     */
    override fun fileRead(file: File): List<Integrante> {
        logger.debug { "Leyendo fichero XML" }

        if (!file.exists() || !file.isFile || !file.canRead()) throw Exceptions.StorageException("El fichero no existe, la ruta especificada no es un fichero o no se tienen permisos de lectura")

        val xml = XML {}

        val xmlString = file.readText() // Leemos cada línea del fichero
        val listaEquipoDTO = xml.decodeFromString <EquipoDTO>(xmlString) // Convertimos el texto anteriormente leido a la clase EquipoDTO, que contiene la lista en la que se almacena cada IntegranteDTO
        val listaIntegrantesDTO = listaEquipoDTO.equipo // De la clase listaEquipoDTO, nos quedamos solo con la lista que contiene los IntegrantesDTO
        val listaIntegrantes = listaIntegrantesDTO.map { it.toModel() } // Mapeamos la lista de DTO para convertir cada elemento a un modelo

        return  listaIntegrantes
    }

    /**
     * Escribe en un fichero dada una lista de [Integrante] y una ruta especificada
     * @param equipo La lista de integrantes
     * @param file El archivo donde se escribira la lista
     */
    override fun fileWrite(equipo: List<Integrante>, file: File) {
        logger.debug { "Escribiendo en fichero XML" }

        if (!file.parentFile.exists() || !file.parentFile.isDirectory) {
            throw Exceptions.StorageException("El directorio padre del fichero no existe")
        }

        val xml = XML {indent = 4}

        val listaIntegrantesDTO: List<IntegranteXmlDTO> = equipo.mapNotNull {
            when (it) {
                is Jugador -> {it.toXmlDTO()}
                is Entrenador -> {it.toXmlDTO()}
                else -> null
            }
        } //Pasamos la lista de Integrantes (modelo) a IntegranteXmlDTO

        val equipoDTO = EquipoDTO(listaIntegrantesDTO) //Instanciamos la clase EquipoDTO, que es la que contiene la lista con los IntegranteXmlDTO

        val xmlString = xml.encodeToString<EquipoDTO>(equipoDTO) //La convertimos en un String

        file.writeText(xmlString) //Escribimos el String en el fichero .xml

    }
}