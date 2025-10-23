const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const { spawn } = require('child_process');
const path = require('path');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Objeto para almacenar los procesos FFmpeg activos por ID de cliente/cámara
const activeFfmpegProcesses = new Map();

// Sirve el archivo HTML estático
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

wss.on('connection', ws => {
    let currentCameraId = null;

    ws.on('message', message => {
        try {
            const data = JSON.parse(message);

            if (data.action === 'start_stream') {
                const { id, rtspUrl } = data;

                if (!id || !rtspUrl) {
                    ws.send(JSON.stringify({ action: 'error', id: id, message: 'Faltan ID o URL RTSP.' }));
                    return;
                }

                // Si ya hay un proceso para este ID, lo eliminamos primero
                if (activeFfmpegProcesses.has(id)) {
                    activeFfmpegProcesses.get(id).kill('SIGINT');
                    activeFfmpegProcesses.delete(id);
                }

                currentCameraId = id;
                console.log(`Iniciando stream para Cámara ID: ${id} con URL: ${rtspUrl}`);

                // --- COMANDO FFmpeg ---
                // Nota: La URL RTSP debe ser ya la URL completa, incluyendo el usuario y contraseña.
                const ffmpegCommand = [
                    '-i', rtspUrl,
                    '-f', 'webm',
                    '-codec:v', 'vp8',
                    '-an', // Sin audio, para simplificar
                    '-cpu-used', '4',
                    '-b:v', '1M',
                    '-crf', '10',
                    '-deadline', 'realtime',
                    '-g', '30',
                    '-r', '20', // Limitar a 20 FPS
                    '-' // Salida a stdout
                ];

                const ffmpeg = spawn('ffmpeg', ffmpegCommand);
                activeFfmpegProcesses.set(id, ffmpeg);

                // Manejar la salida de FFmpeg y etiquetarla con el ID de la cámara antes de enviarla
                ffmpeg.stdout.on('data', data => {
                    // Envía el stream binario tal cual, el cliente sabrá a qué <video> enviarlo
                    if (ws.readyState === WebSocket.OPEN) {
                        // Enviamos los datos binarios al cliente
                        ws.send(data);
                    }
                });

                ffmpeg.stderr.on('data', data => {
                     // Solo para debugging, comenta si el log es muy ruidoso
                     // console.error(`FFmpeg [${id}] stderr: ${data.toString()}`);
                });

                ffmpeg.on('close', code => {
                    console.log(`FFmpeg proceso [${id}] terminado con código ${code}`);
                    activeFfmpegProcesses.delete(id); // Eliminar de los activos
                });

                ffmpeg.on('error', err => {
                    console.error(`Error al iniciar FFmpeg [${id}]:`, err);
                    ws.send(JSON.stringify({ action: 'error', id: id, message: `FFmpeg falló al iniciar. Error: ${err.message}` }));
                    activeFfmpegProcesses.delete(id);
                });

            } else if (data.action === 'stop_stream') {
                const { id } = data;
                if (activeFfmpegProcesses.has(id)) {
                    activeFfmpegProcesses.get(id).kill('SIGINT');
                    activeFfmpegProcesses.delete(id);
                    console.log(`Stream para Cámara ID: ${id} detenido por solicitud del cliente.`);
                }
            }
        } catch (e) {
            console.error('Error al parsear mensaje o en la lógica:', e);
            ws.send(JSON.stringify({ action: 'error', message: 'Error de procesamiento del servidor.' }));
        }
    });

    ws.on('close', () => {
        console.log('Cliente WebSocket desconectado. Deteniendo streams asociados.');
        // Opcional: Podrías detener todos los streams asociados a este cliente si quisieras
    });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Servidor de streaming escuchando en http://localhost:${PORT}`);
});