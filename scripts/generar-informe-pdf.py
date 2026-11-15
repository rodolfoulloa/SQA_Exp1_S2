"""Genera docs/informe-sonarqube-S4.pdf a partir de docs/informe-sonarqube-S4.md y las capturas de Sonar.

Uso:  python scripts/generar-informe-pdf.py      (requiere: pip install reportlab)
Convierte el subconjunto de Markdown que usa el informe (titulos, parrafos, listas, tablas, `codigo`,
**negrita**, *cursiva* y enlaces) y agrega un anexo con las capturas docs/capturas/sonar-*.png.
"""
import html
import pathlib
import re

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (Image, KeepTogether, ListFlowable, ListItem, PageBreak, Paragraph,
                                SimpleDocTemplate, Spacer, Table, TableStyle)

REPO = pathlib.Path(__file__).resolve().parent.parent
ORIGEN = REPO / "docs" / "informe-sonarqube-S4.md"
DESTINO = REPO / "docs" / "informe-sonarqube-S4.pdf"
CAPTURAS = sorted((REPO / "docs" / "capturas").glob("sonar-*.png"))

# Fuentes TrueType de Windows (soportan tildes y simbolos como ≥ y →); si no estan, Helvetica.
FUENTES = pathlib.Path("C:/Windows/Fonts")
if (FUENTES / "arial.ttf").exists():
    pdfmetrics.registerFont(TTFont("Texto", str(FUENTES / "arial.ttf")))
    pdfmetrics.registerFont(TTFont("Texto-B", str(FUENTES / "arialbd.ttf")))
    pdfmetrics.registerFont(TTFont("Texto-I", str(FUENTES / "ariali.ttf")))
    pdfmetrics.registerFont(TTFont("Mono", str(FUENTES / "consola.ttf")))
    pdfmetrics.registerFontFamily("Texto", normal="Texto", bold="Texto-B", italic="Texto-I",
                                  boldItalic="Texto-B")
    TEXTO, NEGRITA, MONO = "Texto", "Texto-B", "Mono"
else:
    TEXTO, NEGRITA, MONO = "Helvetica", "Helvetica-Bold", "Courier"

AZUL = colors.HexColor("#1F3A68")
GRIS = colors.HexColor("#5B6573")
FONDO_TABLA = colors.HexColor("#E8EEF7")
BORDE = colors.HexColor("#B8C2D1")

E = {
    "titulo": ParagraphStyle("titulo", fontName=NEGRITA, fontSize=19, leading=24, textColor=AZUL,
                             spaceAfter=4),
    "sub": ParagraphStyle("sub", fontName=TEXTO, fontSize=10, leading=14, textColor=GRIS),
    "h2": ParagraphStyle("h2", fontName=NEGRITA, fontSize=13.5, leading=17, textColor=AZUL,
                         spaceBefore=14, spaceAfter=6),
    "h3": ParagraphStyle("h3", fontName=NEGRITA, fontSize=11, leading=14, textColor=AZUL,
                         spaceBefore=10, spaceAfter=4),
    "p": ParagraphStyle("p", fontName=TEXTO, fontSize=9.5, leading=13.5, spaceAfter=6),
    "celda": ParagraphStyle("celda", fontName=TEXTO, fontSize=8, leading=10.5),
    "celda_h": ParagraphStyle("celda_h", fontName=NEGRITA, fontSize=8, leading=10.5, textColor=AZUL),
    "pie": ParagraphStyle("pie", fontName=TEXTO, fontSize=8.5, leading=11, textColor=GRIS,
                          alignment=TA_CENTER, spaceBefore=3, spaceAfter=12),
}


def inline(texto):
    """Markdown en linea -> mini-HTML de reportlab (escapando primero el texto)."""
    texto = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r"\1", texto)  # enlaces: solo el texto
    partes = re.split(r"(`[^`]+`)", texto)
    salida = []
    for parte in partes:
        if parte.startswith("`") and parte.endswith("`") and len(parte) > 1:
            salida.append(f'<font face="{MONO}" color="#8A2D3B">{html.escape(parte[1:-1])}</font>')
            continue
        t = html.escape(parte, quote=False)
        t = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", t)
        t = re.sub(r"(?<![\w*])\*(?!\s)(.+?)(?<!\s)\*(?![\w*])", r"<i>\1</i>", t)
        salida.append(t)
    return "".join(salida)


def tabla(filas, ancho):
    cab, cuerpo = filas[0], [f for f in filas[2:]]
    datos = [[Paragraph(inline(c), E["celda_h"]) for c in cab]]
    datos += [[Paragraph(inline(c), E["celda"]) for c in f] for f in cuerpo]
    # Ancho de columna proporcional al largo del texto (con minimo y maximo).
    largos = [max(len(re.sub(r"[`*]", "", f[i])) for f in [cab] + cuerpo) for i in range(len(cab))]
    pesos = [min(max(l, 9), 48) for l in largos]
    total = sum(pesos)
    t = Table(datos, colWidths=[ancho * p / total for p in pesos], repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), FONDO_TABLA),
        ("GRID", (0, 0), (-1, -1), 0.5, BORDE),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4), ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))
    return t


def celdas(linea):
    return [c.strip() for c in linea.strip().strip("|").split("|")]


def convertir(md, ancho):
    story, lineas, i = [], md.splitlines(), 0
    primer_titulo = True
    while i < len(lineas):
        l = lineas[i]
        if not l.strip():
            i += 1
            continue
        if l.startswith("# "):
            story.append(Paragraph(inline(l[2:]), E["titulo"]))
            i += 1
            # Primer parrafo tras el titulo = subtitulo (asignatura, actividad), una linea por renglon.
            sub = []
            while i < len(lineas) and not lineas[i].strip():
                i += 1
            while i < len(lineas) and lineas[i].strip() and not lineas[i].startswith("#"):
                sub.append(inline(lineas[i].strip()))
                i += 1
            if sub and primer_titulo:
                story.append(Paragraph("<br/>".join(sub), E["sub"]))
                story.append(Spacer(1, 4))
                linea = Table([[""]], colWidths=[ancho], rowHeights=[2])
                linea.setStyle(TableStyle([("LINEABOVE", (0, 0), (-1, -1), 1.5, AZUL)]))
                story.append(linea)
            primer_titulo = False
            continue
        if l.startswith("## "):
            story.append(Paragraph(inline(l[3:]), E["h2"]))
            i += 1
            continue
        if l.startswith("### "):
            story.append(Paragraph(inline(l[4:]), E["h3"]))
            i += 1
            continue
        if l.lstrip().startswith("|"):
            filas = []
            while i < len(lineas) and lineas[i].lstrip().startswith("|"):
                filas.append(celdas(lineas[i]))
                i += 1
            t = tabla(filas, ancho)
            # Un parrafo corto que presenta la tabla ("Hallazgos:") no queda separado de ella.
            if story and isinstance(story[-1], Paragraph) and story[-1].getPlainText().rstrip().endswith(":"):
                story.append(KeepTogether([story.pop(), t]))
            else:
                story.append(t)
            story.append(Spacer(1, 8))
            continue
        m = re.match(r"^(\s*)(- |\d+\. )", l)
        if m:
            numerada = m.group(2)[0].isdigit()
            items = []
            while i < len(lineas):
                actual = lineas[i]
                mm = re.match(r"^(\s*)(- |\d+\. )(.*)", actual)
                if mm and len(mm.group(1)) == len(m.group(1)):
                    items.append([mm.group(3).strip()])
                elif actual.strip() and (actual.startswith("  ") and items) and not actual.lstrip().startswith("|"):
                    sub = re.match(r"^\s+- (.*)", actual)
                    if sub and len(actual) - len(actual.lstrip()) > len(m.group(1)):
                        items[-1].append(("sub", sub.group(1).strip()))
                    elif items[-1] and isinstance(items[-1][-1], tuple):
                        tipo, txt = items[-1][-1]
                        items[-1][-1] = (tipo, txt + " " + actual.strip())
                    else:
                        items[-1][0] += " " + actual.strip()
                else:
                    break
                i += 1
            flow = []
            for it in items:
                contenido = [Paragraph(inline(it[0]), E["p"])]
                subs = [x[1] for x in it[1:] if isinstance(x, tuple)]
                if subs:
                    contenido.append(ListFlowable(
                        [ListItem(Paragraph(inline(s), E["p"]), leftIndent=12) for s in subs],
                        bulletType="bullet", start="–", leftIndent=12, bulletFontName=TEXTO))
                flow.append(ListItem(contenido, leftIndent=14))
            story.append(ListFlowable(flow, bulletType="1" if numerada else "bullet",
                                      start=None if numerada else "•", leftIndent=14,
                                      bulletFontName=TEXTO, bulletFontSize=9))
            continue
        parrafo = []
        while i < len(lineas) and lineas[i].strip() and not re.match(r"^(#|\||\s*- |\d+\. )", lineas[i]):
            parrafo.append(lineas[i].strip())
            i += 1
        story.append(Paragraph(inline(" ".join(parrafo)), E["p"]))
    return story


def anexo(ancho):
    story = [PageBreak(), Paragraph("Anexo · Capturas de SonarQube", E["h2"])]
    for n, img in enumerate(CAPTURAS):
        nombre = img.stem.replace("sonar-", "").split("-", 1)[1].replace("-", " ")
        w = ancho * 0.76  # dos capturas por pagina
        h = w * 646 / 800
        story.append(KeepTogether([
            Image(str(img), width=w, height=h),
            Paragraph(f"Figura {n + 1}. {nombre.capitalize().replace('Api ', 'API ').replace('Web ', 'Web: ')} —<font face='{MONO}'>{img.name}</font>",
                      E["pie"]),
        ]))
    return story


def pie_pagina(canvas, doc):
    canvas.saveState()
    canvas.setFont(TEXTO, 7.5)
    canvas.setFillColor(GRIS)
    canvas.drawString(doc.leftMargin, 1.2 * cm, "PixelMeet · ISY2202 · Experiencia 2, Semana 4 · Informe SonarQube")
    canvas.drawRightString(A4[0] - doc.rightMargin, 1.2 * cm, f"Página {doc.page}")
    canvas.restoreState()


def main():
    doc = SimpleDocTemplate(str(DESTINO), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm,
                            topMargin=1.8 * cm, bottomMargin=2 * cm,
                            title="Informe · Análisis de código estático con SonarQube",
                            author="Rodolfo Ulloa", subject="ISY2202 Experiencia 2 Semana 4")
    ancho = A4[0] - doc.leftMargin - doc.rightMargin
    story = convertir(ORIGEN.read_text(encoding="utf8"), ancho) + anexo(ancho)
    doc.build(story, onFirstPage=pie_pagina, onLaterPages=pie_pagina)
    print(DESTINO)


if __name__ == "__main__":
    main()
