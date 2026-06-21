from flask import Flask, request, jsonify
from flask_mail import Mail, Message
from flask_cors import CORS
from flask import Flask, jsonify
import mysql.connector 

app = Flask(__name__)
CORS(app)#este cors permite que el servidor de python acepte solicitudes desde cualquier origen, lo cual es útil durante el desarrollo para evitar problemas de CORS cuando el frontend y el backend están en dominios diferentes. Sin embargo, en producción, es recomendable configurar CORS para permitir solo los orígenes necesarios por seguridad.

# CONFIGURACIÓN DEL SERVIDOR DE CORREO 
app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 587
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USERNAME'] = 'manueldiazpena7@gmail.com' #autenticacion para poder enviar el correo 
app.config['MAIL_PASSWORD'] = 'ucfcjsykhwxpubhl' 
app.config['MAIL_DEFAULT_SENDER'] = 'tu-correo@gmail.com'
#aqui le decimos a pythom como conectarse a la las oficinas de correos de goolge
#esto es un protoclo estandar para enviar correos
#el puerto 587 es el puerto estandar para conexiones seguras con TLS

mail = Mail(app)

@app.route('/api/enviar-enlace', methods=['POST'])#punto de encuento en este caso al usar el restamplte donde le nviamos correo y link de recuperacion a python este se encarga de recibir esos datos en esta ruta y luego enviar el correo al usuario con el link de recuperacion
def enviar_enlace():
    try:
        data = request.get_json()
        email_destino = data.get('email')
        enlace_recuperacion = data.get('link')#aqui python desempaca lo que java envio

        
        msg = Message('Restablecer Acceso - Compuedu',#aqui se crea el obejto message creando asusnto y el destinatario
                      recipients=[email_destino])
        
        # Cuerpo del mensaje limpio de tildes/eñes para la prueba inicial
        # Diseño HTML profesional
        msg.html = f"""
        <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;">
            <div style="background-color: #343a40; padding: 20px; text-align: center;">
                <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Compuedu</h1>
            </div>
            <div style="padding: 30px; background-color: #ffffff; line-height: 1.6;">
                <h2 style="color: #333333;">Hola,</h2>
                <p style="color: #666666; font-size: 16px;">
                    Has solicitado restablecer tu contraseña para acceder a nuestra plataforma académica. 
                    No te preocupes, estamos aquí para ayudarte.
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="{enlace_recuperacion}" 
                       style="background-color: #343a40; color: #ffffff; padding: 15px 25px; text-decoration: none; border-radius: 50px; font-weight: bold; display: inline-block; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                       Restablecer Contraseña
                    </a>
                </div>
                <p style="color: #888888; font-size: 13px;">
                    Si no solicitaste este cambio, puedes ignorar este correo de forma segura. El enlace expirará pronto.
                </p>
            </div>
            <div style="background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #999999;">
                &copy; 2026 Compuedu - Herramientas Académicas.
            </div>
        </div>
        """
        
        mail.send(msg)#es el orden final python se conecta a google se loguea y entrega el mensaje
        return jsonify({"status": "success", "message": "Correo enviado correctamente"}), 200#convierte un diccionario python a json para que java entienda la respuesta y el 200 indica no hubo errores
    

    except Exception as e:#captura cualquier erro que ocurra durante el proceso de envio del correo y lo muestra en la consola de python para que puedas debuguear el error real, ademas de enviar una respuesta de error a java con el mensaje del error
        # Esto imprimirá el error real en tu terminal de Python (ej: error de login)
        print(f"DEBUG ERROR: {str(e)}")
        return jsonify({"status": "error", "message": str(e)}), 500#indica el erro real que ocurrio en python para que java lo reciba y puedas mostrar un mensaje de error mas amigable al usuario o tomar acciones dependiendo del error




def get_db_connection():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="", # Tu contraseña de MySQL
        database="compuedu" 
    )

@app.route('/api/stats/institucion/<int:creador_id>', methods=['GET'])
def get_stats(creador_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        
        # 1. Distribución por estado
        query_estados = "SELECT estado, COUNT(*) as cantidad FROM convocatorias WHERE creador_id = %s GROUP BY estado"
        cursor.execute(query_estados, (creador_id,))
        distribucion = cursor.fetchall()
        
        # 2. Alertas de vencimiento (Opcional, para que no mande error el HTML)
        query_urgentes = "SELECT titulo, DATEDIFF(fecha_fin, CURDATE()) as dias_restantes FROM convocatorias WHERE creador_id = %s AND fecha_fin > CURDATE() ORDER BY dias_restantes ASC LIMIT 3"
        cursor.execute(query_urgentes, (creador_id,))
        urgentes = cursor.fetchall()

        cursor.close()
        conn.close()
        
        return jsonify({
            "distribucion_estado": distribucion,
            "urgentes": urgentes
        })
    except Exception as e:
        print(f"Error en stats: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000, debug=True)