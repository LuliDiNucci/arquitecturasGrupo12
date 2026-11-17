#!/usr/bin/env python3
"""Carga un ejemplo completo contra Eventos local, usando solo la biblioteca estándar."""
import json
import sys
from datetime import datetime, timedelta
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

BASE = sys.argv[1].rstrip('/') if len(sys.argv) > 1 else 'http://localhost:8081'


def llamar(metodo, ruta, datos=None):
    payload = json.dumps(datos).encode('utf-8') if datos is not None else None
    request = Request(
        BASE + ruta,
        data=payload,
        headers={'Content-Type': 'application/json'},
        method=metodo,
    )
    try:
        with urlopen(request, timeout=10) as respuesta:
            contenido = respuesta.read().decode('utf-8')
            return json.loads(contenido) if contenido else None
    except HTTPError as error:
        print(f'Error HTTP {error.code} en {metodo} {ruta}:')
        print(error.read().decode('utf-8', errors='replace'))
        raise
    except URLError as error:
        print(f'No se pudo conectar con {BASE}: {error}')
        raise


def main():
    # Los ejemplos se crean de cero y cada ejecucion suma registros nuevos.
    organizador = llamar('POST', '/organizadores', {
        'nombre': 'Producciones Sierra', 'email': 'contacto@ejemplo.com'
    })
    participante = llamar('POST', '/participantes', {
        'nombre': 'Banda Ejemplo', 'tipo': 'ARTISTA'
    })
    lugar = llamar('POST', '/lugares', {
        'nombre': 'Estadio Municipal', 'direccion': 'Av. Central 150',
        'ciudad': 'Tandil', 'latitud': -37.321, 'longitud': -59.137
    })
    numerado = llamar('POST', f'/lugares/{lugar["id"]}/sectores', {
        'nombre': 'Platea', 'numerado': True, 'capacidad': 2
    })
    campo = llamar('POST', f'/lugares/{lugar["id"]}/sectores', {
        'nombre': 'Campo', 'numerado': False, 'capacidad': 500
    })
    llamar('POST', f'/sectores/{numerado["id"]}/asientos', {
        'asientos': [{'fila': 'A', 'numero': 1}, {'fila': 'A', 'numero': 2}]
    })
    fecha = (datetime.now() + timedelta(days=60)).replace(microsecond=0).isoformat()
    evento = llamar('POST', '/eventos', {
        'nombre': 'Recital de prueba', 'descripcion': 'Evento para probar TicketYa',
        'tipo': 'RECITAL', 'organizadorId': organizador['id'],
        'lugarId': lugar['id'], 'participantesIds': [participante['id']],
        'fechaHora': fecha, 'altaDemanda': False
    })
    llamar('PUT', f'/eventos/{evento["id"]}/precios', {
        'precios': [
            {'sectorId': numerado['id'], 'precio': 35000},
            {'sectorId': campo['id'], 'precio': 20000}
        ]
    })
    publicado = llamar('PATCH', f'/eventos/{evento["id"]}/publicar')
    print(json.dumps({
        'mensaje': 'Evento publicado en la base local',
        'eventoId': evento['id'], 'lugarId': lugar['id'],
        'sectorNumeradoId': numerado['id'], 'sectorCampoId': campo['id'],
        'estado': publicado['estado'],
        'consulta': BASE + f'/eventos/{evento["id"]}'
    }, indent=2, ensure_ascii=False))


if __name__ == '__main__':
    main()
