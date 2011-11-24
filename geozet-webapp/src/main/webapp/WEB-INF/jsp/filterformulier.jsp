<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="nl.geozet.common.CoreResources, nl.geozet.common.StringConstants,
		nl.geozet.common.NumberConstants, nl.eleni.gcc.vpziek.beans.ActieveZiektenBean,
		java.util.Arrays, java.util.Collections, java.util.List;" %>


<%-- Deze component is het filterformulier --%>
<%
    // resource bundel laden voor de tekst en headers
    CoreResources RESOURCES = new CoreResources(this
            .getServletContext().getInitParameter(
                    StringConstants.CONFIG_PARAM_RESOURCENAME.code));

    //om het formulier "sticky" te maken

    //coords
    String xcoord = (request
            .getParameter(StringConstants.REQ_PARAM_XCOORD.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_XCOORD.code) : "";
    String ycoord = (request
            .getParameter(StringConstants.REQ_PARAM_YCOORD.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_YCOORD.code) : "";

    //gevondenadres
    String gevonden = (request
            .getParameter(StringConstants.REQ_PARAM_GEVONDEN.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_GEVONDEN.code)
            : "onbekend";

    //gevraagdadres
    String adres = (request
            .getParameter(StringConstants.REQ_PARAM_ADRES.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_ADRES.code) : "";

    //straal
    String selected = (request
            .getParameter(StringConstants.REQ_PARAM_STRAAL.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_STRAAL.code)
            : NumberConstants.OPENLS_ZOOMSCALE_STANDAARD.toString();

    //font size
    String selectedFont = (request
            .getParameter(StringConstants.REQ_PARAM_FONTSIZE.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_FONTSIZE.code)
            : NumberConstants.DEFAULT_FONT_SIZE.toString();

	// kleuren schema
    String selectedColor = (request
            .getParameter(StringConstants.REQ_PARAM_COLORSCHEME.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_COLORSCHEME.code)
            : StringConstants.DEFAULT_COLORSCHEME.toString();

    //filter
    String filterUsed = request
            .getParameter(StringConstants.REQ_PARAM_EXPLICITUSEFILTER.code);
    String[] chkBoxesChecked = request
            .getParameterValues(StringConstants.REQ_PARAM_FILTER.code);
    List<String> elements = ((ActieveZiektenBean)application.getAttribute("actieveziekten")).getElements();
    List<String> checkedList = elements;

     if (null != filterUsed && filterUsed.equalsIgnoreCase("true")) {
        // als REQ_PARAM_USEFILTER == true filter instellen volgen request params
         checkedList = (chkBoxesChecked != null) ? Arrays
                 .asList(chkBoxesChecked) : Collections.EMPTY_LIST;
      }

    // core only..
    String coreonly = (request
            .getParameter(StringConstants.REQ_PARAM_COREONLY.code) != null) ? request
            .getParameter(StringConstants.REQ_PARAM_COREONLY.code)
            : "false";
%>

<form action="#" class="geozetRefine">
	<fieldset>
		<legend><%=RESOURCES.getString("KEY_FILTERFORM_LEGEND_ONDERWERP")%></legend>
		<p class="intro"><%=RESOURCES.getString("KEY_FILTERFORM_INTRO")%></p>
		<ul class="geozetFilter">
			<%
			    for (String cat : elements) {
			%>
			<li class="ziekte">
				<input name="filter" value="<%=cat%>" type="checkbox" <%if (checkedList.contains(cat)) {%>	checked="checked" <%}%> id="geo-<%=cat.replace(" ","-")%>" />
				<label for="geo-<%=cat.replace(" ","-")%>"><%=cat%></label>
			</li>
			<%
			    }
			%>
		</ul>
	</fieldset>

	<%-- verschuif functie TODO implementatie--%>
	<%--
	<fieldset>
		<legend><%=RESOURCES.getString("KEY_FILTERFORM_LEGEND_VERPLAATS")%></legend>
		<label><%=RESOURCES.getString("KEY_FILTERFORM_LABEL_VERPLAATS")%></label>
		<ul>
			<li><label><input type="radio" value="noord" name="verplaatsrichting" />Noord</label></li>
			<li><label><input type="radio" value="oost" name="verplaatsrichting" />Oost</label></li>
			<li><label><input type="radio" value="zuid" name="verplaatsrichting" />Zuid</label></li>
			<li><label><input type="radio" value="west" name="verplaatsrichting" />West</label></li>
		</ul>
	 </fieldset>
	 --%>

	<%-- afstand/straal functie --%>
	<fieldset>
		<legend><%=RESOURCES.getString("KEY_FILTERFORM_LEGEND_STRAAL")%></legend>
		<label for="<%=StringConstants.REQ_PARAM_STRAAL%>"><%=RESOURCES.getString("KEY_FILTERFORM_LABEL_STRAAL")%>
			<select name="<%=StringConstants.REQ_PARAM_STRAAL%>" id="<%=StringConstants.REQ_PARAM_STRAAL%>">
				<%-- TODO: evt. dynamisch maken op basis van de waarden in nl.geozet.common.NumberConstants --%>
				<option value="300000" <%if (("300000").equals(selected)) {%>
					selected="selected" <%}%>>300 km (heel Nederland)</option>
				<option value="150000" <%if (("150000").equals(selected)) {%>
					selected="selected" <%}%>>150 km</option>
				<option value="50000" <%if (("50000").equals(selected)) {%>
					selected="selected" <%}%>>50 km</option>
				<option value="25000" <%if (("25000").equals(selected)) {%>
					selected="selected" <%}%>>25 km</option>
				<option value="10000" <%if (("10000").equals(selected)) {%>
					selected="selected" <%}%>>10 km</option>
				<option value="3000" <%if (("3000").equals(selected)) {%>
					selected="selected" <%}%>>3 km</option>
				<option value="1500" <%if (("1500").equals(selected)) {%>
					selected="selected" <%}%>>1,5 km</option>
				<!-- option value="500" <%if (("500").equals(selected)) {%>
					selected="selected" <%}%>>500 m</option -->
			</select>
		</label>
	</fieldset>

	<%-- fontsize /kleuren schema--%>
	<fieldset>
		<legend><%=RESOURCES.getString("KEY_FILTERFORM_LEGEND_OPMAAK")%></legend>
		<p class="intro"><%=RESOURCES.getString("KEY_FILTERFORM_LEGEND_OPMAAK_INTRO")%></p>
		<label for="<%=StringConstants.REQ_PARAM_FONTSIZE%>"><%=RESOURCES.getString("KEY_FILTERFORM_LABEL_FONT_SIZE")%>
			<select name="<%=StringConstants.REQ_PARAM_FONTSIZE%>" id="<%=StringConstants.REQ_PARAM_FONTSIZE%>">
				<option value="10" <%if (("10").equals(selectedFont)) {%>
						selected="selected" <%}%>>10</option>
				<option value="12" <%if (("12").equals(selectedFont)) {%>
						selected="selected" <%}%>>12</option>
				<option value="14" <%if (("14").equals(selectedFont)) {%>
						selected="selected" <%}%>>14</option>
				<option value="16" <%if (("16").equals(selectedFont)) {%>
						selected="selected" <%}%>>16</option>
			</select>
		</label>

		<label for="<%=StringConstants.REQ_PARAM_COLORSCHEME%>"><%=RESOURCES.getString("KEY_FILTERFORM_LABEL_COL_SCHEME")%>
			<select name="<%=StringConstants.REQ_PARAM_COLORSCHEME%>" id="<%=StringConstants.REQ_PARAM_COLORSCHEME%>">
				<option value="<%=StringConstants.COLOURSCHEME_COLOUR.code%>" <%if ((StringConstants.COLOURSCHEME_COLOUR.code).equals(selectedColor)) {%>
					selected="selected" <%}%>>kleur</option>
				<option value="<%=StringConstants.COLOURSCHEME_MONO.code%>" <%if ((StringConstants.COLOURSCHEME_MONO.code).equals(selectedColor)) {%>
					selected="selected" <%}%>>één kleur</option>
				<option value="<%=StringConstants.COLOURSCHEME_BLACKWHITE.code%>" <%if ((StringConstants.COLOURSCHEME_BLACKWHITE.code).equals(selectedColor)) {%>
					selected="selected" <%}%>>zwart/wit</option>
				<option value="<%=StringConstants.COLOURSCHEME_GREYSCALE.code%>" <%if ((StringConstants.COLOURSCHEME_GREYSCALE.code).equals(selectedColor)) {%>
					selected="selected" <%}%>>grijstinten</option>
			</select>
		</label>
	</fieldset>

	<p class="button">
		<%-- verborgen velden --%>
		<input type="hidden" name="<%=StringConstants.REQ_PARAM_ADRES%>"
			id="<%=StringConstants.REQ_PARAM_ADRES%>" value="<%=adres%>" /> <input
			type="hidden" name="<%=StringConstants.REQ_PARAM_XCOORD%>"
			id="<%=StringConstants.REQ_PARAM_XCOORD%>" value="<%=xcoord%>" /> <input
			type="hidden" name="<%=StringConstants.REQ_PARAM_YCOORD%>"
			id="<%=StringConstants.REQ_PARAM_YCOORD%>" value="<%=ycoord%>" /> <input
			type="hidden" name="<%=StringConstants.REQ_PARAM_GEVONDEN%>"
			id="<%=StringConstants.REQ_PARAM_GEVONDEN%>" value="<%=gevonden%>" />

		<input type="hidden"
			name="<%=StringConstants.REQ_PARAM_EXPLICITUSEFILTER%>"
			id="<%=StringConstants.REQ_PARAM_EXPLICITUSEFILTER%>" value="true" />
		<input type="hidden" name="<%=StringConstants.REQ_PARAM_COREONLY%>"
			id="<%=StringConstants.REQ_PARAM_COREONLY%>" value="<%=coreonly%>" />

		<button type="submit">
			<span><%=RESOURCES.getString("KEY_FILTERFORM_SUBMIT")%></span>
		</button>
	</p>
</form>
