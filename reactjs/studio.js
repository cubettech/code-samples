import React, { Component } from 'react';
import { Prompt } from 'react-router-dom';
import { createStructuredSelector } from 'reselect';
import { connect } from 'react-redux';
import toWav from 'audiobuffer-to-wav';
import API from '../../../helpers/API';
import { MicrophoneRecorder } from '../../../components/Audio/MicrophoneRecorder';
import AudioContext from '../../../components/Audio/AudioContext';
import LayoutContentWrapper from '../../../components/utility/layoutWrapper';
import LayoutContent from '../../../components/utility/layoutContent';
import Popconfirms from '../../../components/feedback/popconfirm';
import Modals from '../../../components/feedback/modal';
import PopconfirmWrapper from '../../../components/feedback/popconfirm.style';
import ModalStyle from '../../../components/uielements/styles/modal.style';
import WithDirection from '../../../config/withDirection';
import Scrollbar from '../../../components/utility/customScrollBar.js';
import { getProfile } from '../../../redux/auth/selectors';
import actions from '../../../redux/Studio/actions';
import { getProjectDetails} from '../../../redux/Studio/selectors';
import { socket } from "../socket";
import { MovieInfo } from './movieInfo';

const WaveformPlaylist = window.WaveformPlaylist;
const { fetchProjectInfo, submitFile, saveRecording, getSavedDubTracks } = actions;
let $ = window.$;
let timeStartAt = 0;
let recordStartTime = 0;
let recordEndTime = 0;
let ee, playlist, pId, cId;
let totalTracks = 0;
let tracksDeleted = 0;
let charaId = null;


const Popconfirm = props => (
    <PopconfirmWrapper>
      <Popconfirms {...props} />
    </PopconfirmWrapper>
  );

const isoModal = ModalStyle(Modals);
const Modal = WithDirection(isoModal);
const confirm = Modals.confirm;


/*
 Function to render takes
*/
const Take = props => {
    const take = props.take;
    const numOfSubtitles = take.filter((subtitle)=> subtitle.character && subtitle.character._id == charaId);
    if(numOfSubtitles.length > 0){
        return <tr className="ant-table-row  ant-table-row-level-0">
                <td className="" onClick={(e) => props.changeTake(e, take[take.length -1].take)}>
                    <span className="ant-table-row-indent indent-level-0" style={{paddingLeft: '0px'}}></span>
                    <a href="#"><p>{`Take ${take[take.length -1].take}`}</p></a></td>
                <td onClick={(e) => props.changeTake(e, take[take.length -1].take)}><a href="#"><p>{props.getTakeStatus(take[take.length -1].take)}</p></a></td>
            </tr>
    }else{
        return null;
    }
        
}

/*
 Function to render Movie info
*/
const MovieInfo = (props) => {
    return (
      <section className="movie-info">
          <h3>{props.projectTitle} {`Take: ${props.takeNo}`}</h3>
              <span className="watermark-title">{props.me ? props.me.username : ''}</span>
              <div className="video-controls" style={{position:'relative'}}>
                  <div className="left">
                  {!props.isRecording && <a href="#" title="Start Recording" onClick={(e) =>props.startRecording(e)}>
                      <i className="sprite-record"></i></a> }
                  {props.isRecording && <a href="#" title="Stop Recording" onClick={(e) =>props.stopRecording(e)}>
                      <i className="sprite-recording"></i></a> }
                      <canvas ref="canvas" width="30" height="30"></canvas>
                  </div>
                  <div className="left timecode">
                  </div>
                  <div className="center">
                      <ul className="clearfix">
                          <li><a href="#" title="Previous Take" onClick={(e) => props.switchTake(e, 'prev')}><i className="sprite-previous"></i></a></li>
                          {!isPlaying && <li><a href="#" title="Play" onClick={(e) =>props.play(e)}>
                              <i className="sprite-play"></i></a></li>}
                          {isPlaying && <li><a href="#" title="Pause" onClick={(e) =>props.pause(e)}>
                              <i className="sprite-pause"></i></a></li>}
                          <li><a href="#" title="Stop" onClick={(e) =>props.stop(e)}><i className="sprite-stope"></i></a></li>
                          <li><a href="#" title="Next Take" onClick={(e) => props.switchTake(e, 'next')}><i className="sprite-next"></i></a></li>
                      </ul>
                  </div>                      
              </div>
      </section>
    )
}
  

class Studio extends Component  {

  constructor(props){
    super(props);           
  }

  state = {
    volume: 0,
    project: false,
    subtitles: false,
    trackLoaded: false,
    defaultTracks: 1,
    splits: [],
    takeNo: 1,
    currTake: false,
    prevTake: false,
    isRecording: false,
    isPlaying: false,
    mastervolumechanged: false,
    microphoneRecorder  : null,
    takesAttended: [],
    character: false,
    isSubmit: false,
    isBlocked: false,
    isSave: false,
    savedTracks: false,
    playerMode: 'cursor',
    isTrimDisabled: true,
    trackLoaderMsg: 'Loading track...',
    history: [],
    takeAssigned: [],
    currTakeIndx: 0,
  };

  componentWillReceiveProps(nextProps){
    if(nextProps && nextProps.project != this.props.project){
        this.setState({project: nextProps.project},
        function(){
            /*
            Loading the default takes after the project info is loaded
            */
            this.loadAssets();
        });
        let takeArr = [];
        /*
         Creating array of takes
        */
        if(nextProps.project.takes){
            for (var key in nextProps.project.takes) {
                if (nextProps.project.takes.hasOwnProperty(key)) {
                    var val = nextProps.project.takes[key];
                    takeArr.push(val);
                }
            }
        }
    }
  }

  componentDidMount(){
    let { me } = this.props;
    pId = this.props.projectID;
    cId = charaId = this.props.characterId;
    this.props.fetchProjectInfo(pId, cId);
    /*
    Listening socket for characher update
    */
    socket.on(`${me._id} CHECK_VA_STATUS`, (data) => {
        let { takeNo } = this.state;
        socket.emit ('UPDATE_DIRECTOR', {project: pId, take: takeNo});
    });    

    /*
    Initialising the playlist starts here
    */
    this.canvasCtx = this.refs.canvas.getContext('2d');
    this.averaging = 0.95;
    this.canvasCtx.fillStyle = '#00FF48';
    this.canvasCtx.clearRect(0, 0, this.canvasCtx.canvas.width, this.canvasCtx.canvas.height);
    this.canvasCtx.fillRect(0, this.canvasCtx.canvas.height * (1 - 0.05), this.canvasCtx.canvas.width, this.canvasCtx.canvas.height);
    const analyser = AudioContext.getAnalyser();
    const options = { audioBitsPerSecond: 128000,
                        mimeType : 'audio/webm;codecs=opus'};
    this.setState({
        analyser            : analyser,
        microphoneRecorder  : new MicrophoneRecorder(this.onStart, this.onStop, this.onData, options),
        canvasCtx           : this.canvasCtx
    });
    
    const that = this;
    
    /*
    Initialising playlist 
    */
    playlist = WaveformPlaylist.init({
        samplesPerPixel: 2048,
        mono: true,
        waveHeight: 60,
        container: this.refs.playlist,
        state: 'cursor',
        colors: {
            waveOutlineColor: '#e1e2e2',
            timeColor: 'grey',
            fadeColor: 'black'
        },
        timescale: true,
        controls: {
            show: true, //whether or not to include the track controls
            width: 115, //width of controls in pixels

        },
        seekStyle: 'fill',
        zoomLevels: [128, 256, 512, 1024, 2048, 4096]
    });

    if (this.refs.playlist) {
        this.bindEvents();
    }

    /*
    Initialising the playlist ends here
    */
    const container = $("#playlist");
    container.on("click", ".sprite-close", function () {
        that.clearAll();        
    });

    this.toggleEventListner(true);
  }

  componentWillUnmount(){
    this.toggleEventListner(false);
  }

    /*
    Binging all playlist event
    */
  bindEvents = () => {
    ee = playlist.getEventEmitter();
    ee.on("select", this.handleSelect);
    ee.on('onDelete', this.handleOnDelete);
    ee.on('onUndoDelete', this.handleOnUndoDelete);
    ee.on('onClearAll', this.handleOnClearAll);
    ee.on('onCutSuccess', this.handleOnCut);
    ee.on('onDeleteError', this.handleOnDeleteError);
    ee.on('audioBuffersRendered', this.handleBuffer);
  }

 loadAssets = () => {
    const that = this;
    let { project, takeNo } = this.state;
    socket.emit ('UPDATE_DIRECTOR', {project: pId, take: takeNo});
    const character = project.characters.filter(char => this.props.characterId == char._id);
    const currTake = project.takes[takeNo];

    this.setState({
        character: character[0] || false,
        currTake: currTake,
        prevTake: takeNo > 1 ? project.takes[takeNo-1] : false,
    },
    function(){
        that.loadSavedTracks();
    });  
  }

  loadSavedTracks = () => {
    
    this.setState({splits: []});
    const splits = [];
    const that = this;
    const takesAttended = [];
    let recordingList = [];
    
    pId = this.props.projectID;
    cId = this.props.characterId;

    /*
    Fetching auido data for the project
    */
    API.characters.fetchAudio(pId, cId).then((res) => {
        recordingList = res.recordings;
        if(recordingList.length == 0){
            that.setState({trackLoaded : true, takesAttended});
            return false;
        }
        that.setState({trackLoaderMsg: 'Loading saved recordings...'});
        const takeNos = Object.keys(recordingList);
        takeNos.map((takeNo) =>{
            const takNo = parseInt(takeNo, 10);
            if(recordingList[takNo].length > 0){
                takesAttended.push(takNo+1);
            }
        });

        const recordings = recordingList[this.state.takeNo -1] || [];
        if(recordings.length == 0){
            this.setState({trackLoaded : true, takesAttended, splits});
            return false;
        }

        /*
        Loading all saved tracks into the playist, and formating all data as per the input
        */
        const records = recordings.map((element, index) => {
            const track = {
                src: element.file,
                original: false,
                border: 1,
                name: 'Records',
                start: element.start,
                cuein: element.cueIn,
                cueout: element.cueOut,
            }
            /*
            Setting up data for play list input
            */
            const split = {
                part: element.part,
                start: element.start,
                cuein: element.cueIn,
                cueout: element.cueOut,
                end: element.end
            }
            splits.push(split);
            totalTracks += 1;
            
            return track;
        });
        playlist.load(records)
        .then(function () {
            that.setState({trackLoaded : true, takesAttended, splits});
        }).catch((err) =>{
            console.log(err);
        });
    });
  }

  toggleEventListner = (enable) =>{
    if(enable){
        window.addEventListener("keydown", this.handlekeyDown);
    }else{
        window.removeEventListener("keydown", this.handlekeyDown);
    }
  }

/*
Handling keyboard events for playlist
*/

  handlekeyDown = (e) => {
    e.preventDefault();
    const { isPlaying, isRecording } = this.state;
    switch (e.keyCode) {
        case 32:// key Spacebar - To Play/Pause video
                if (!isPlaying) {
                    this.play(e);
                } else {
                    this.pause(e);
                }
                break;
        case 82:// key R - To start/Stop recording
                if(isRecording){
                    this.stopRecording(e);
                }else{
                    this.startRecording(e);
                }                    
                break;
        case 46: // key Delete - To delete a recording
                this.deleteRecord();
                break;
        case 90: // key ctrl+z - To undo recent actions
                if (e.keyCode == 90 && e.ctrlKey) {
                    ee.emit("undo");
                };
                break;
        default: break;
    }
  }

  onStart = () => {
    // Its a dummy function
  }

  clearAll = () => {
    confirm({
        title: 'Clear All',
        content:'This will clear all the recordings. Do you still want to proceed?',
        onOk() {
            ee.emit("clearAll");
            totalTracks = 0;
        },
        onCancel() {},
        okText: 'Yes',
        cancelText: 'No'
    });
 }

 handleOnDeleteError = (err) => {
    Modals.warning({
        title: '',
        content:'You have not selected any track!',
        okText: 'OK',
        cancelText: 'Cancel'
    });
}

  handleOnDelete = (track) =>{
    const splits = Object.assign([], this.state.splits);
    splits.splice(track.userIndex-1 ,1);
    this.setState({splits});
  }

  handleOnUndoDelete = (history) => {
    const track = history.track;
    const splits = Object.assign([], this.state.splits); 
    const split = {
        part: track.part,
        file: track.fileId,
        take: track.take,
        start: track.startTime,
        end: track.endTime,
        cuein: track.cueIn,
        cueout: track.cueOut,
    }
    splits.push(split);
    totalTracks += 1;   
    this.setState({splits});
  }

  handleOnClearAll = () => {
    let splits = Object.assign([], this.state.splits);
    splits = [];
    this.setState({splits});
  }

  handleOnCut = (track) =>{
    const splits = Object.assign([], this.state.splits);
    const split = {
        part: track.part,
        file: '',
        take: track.take,
        start: track.startTime,
        end: track.endTime,
    }
    splits.push(split);
    totalTracks += 1;
    this.setState({
        splits,
    });
  }

  handleSelect = (start, end) =>{
    if(start < end) {
        this.setState({isTrimDisabled: false});
    }else {
        this.setState({isTrimDisabled: true});
    }
  }  

  play = e =>{
    e.preventDefault();
    if(this.state.trackLoaded){
        ee.emit("automaticscroll", true);
        ee.emit("play");        
        this.setState({isPlaying: true});    
    }    
  }

  pause = e => {
    e.preventDefault();
    if(this.state.isRecording){
        this.stopRecording(e);
    }
    ee.emit("pause");
    this.setState({isPlaying: false});
  }

  stop = e => {
    e.preventDefault();
    if(this.state.isRecording){
        this.stopRecording(e);
    }
    ee.emit("stop");
    setTimeout( function() { 
        ee.emit('select', 0, 0);
    },10);
    this.setState({isPlaying: false});
  }

  changeTake = (e, take) => {
    e.preventDefault();
    totalTracks = 0;
    const { project } = this.state;
    ee.emit("clear");
    const that = this;
    const currTake = project.takes[take];    
    const preTake = take > 1 ? project.takes[take-1]: false;
    
    this.setState({
        trackLoaderMsg: 'Loading tracks...',
        trackLoaded: false,
        takeNo: take,
        currTake: currTake,
        prevTake: preTake,
    }, function(){
        that.loadAssets();
    });
}


startRecording = e => {
    e.preventDefault();
    timeStartAt = new Date().getTime()/1000;
    recordStartTime += recordEndTime;
    if(!this.state.isRecording){
        this.setState({
            isRecording: true,
        });
    }
}

stopRecording = e => {
    e.preventDefault();
    this.setState({
        isRecording: false,
    });
    recordEndTime = (new Date().getTime()/1000) - timeStartAt;
}

onData = blob => { 
    const volume = AudioContext.getVolume();
    this.canvasCtx.clearRect(0, 0, this.canvasCtx.canvas.width, this.canvasCtx.canvas.height);
    this.canvasCtx.fillRect(0, this.canvasCtx.canvas.height * (1 - volume), this.canvasCtx.canvas.width, this.canvasCtx.canvas.height);
}

onStop = blobObject => {
    /*
    converting recorded audio raw data into wav file
    */ 
    var blob = blobObject.blob;
    const track = {
        src: blob,
        original: false,
        border: 1,
        name: 'Records',
        start: recordStartTime,
        take: this.state.takeNo,
    }
    playlist.load([track])
    .then(function () {

    }).catch((err) =>{
        console.log(err);
    });

    ee.emit("automaticscroll", true);   

    const { history } = this.state;
    this.setState({
        history,
        blobObject: blobObject.blob,
        blobURL: blobObject.blobURL
    });
}

submitWork = (e, type) =>{
    e.preventDefault();
    const isSave = type === 'save' ? true : false;
    this.setState({isSubmit: true, isSave });
    setTimeout(function(){
        ee.emit('renderAudioBuffers');
    },0)
}

stopLoader = () =>{
    ee.emit("clearHistory");
    this.setState({isSubmit: false});
}

changesUpdated = () => {
    this.setState({isSubmit: false, trackLoaded: false });
    ee.emit("clearAll");
    ee.emit("automaticscroll", false);
    ee.emit("clearHistory");
    this.loadSavedTracks();
}

/*
 Handling buffer data
*/
handleBuffer = (tracks) =>{
    let fileArr = [];
    let wavArr = tracks.map((track, index) =>
        new Blob([toWav(track.buffer)], { type: "octet/stream" })
    );
    
    for(let i=0; i < tracks.length; i++){
        fileArr.push({
            fileName:  `part${i+1}.wav`,
            fileType:  "octet/stream",
        })
    }

    const { project, submitFile, saveRecording, recordings } = this.props;
    const { character, isSave, takeNo } = this.state;

    const data = new Object();
    data.projectId = project.project._id;
    data.characterId = character._id;
    data.takeNo = takeNo;
    const splits = Object.assign([], this.state.splits);

    data.splits = splits.map((split, index) => {
        split.start = tracks[index].start;
        split.end = tracks[index].end;
        split.cueIn = tracks[index].cueIn;
        split.cueOut = tracks[index].cueOut;
        if(split.file != ""){
            fileArr[index] = false;
            wavArr[index] = false;
        }
        return split;
    });

    let takeLength = 0;
    if(project.takes){
        for (var key in project.takes) {
            if (project.takes.hasOwnProperty(key)) {
                takeLength += 1;
            }
        }
    }
    /*
    Saving recorded data
    */
    saveRecording(fileArr, wavArr, data, takeLength, this.changesUpdated);
    
}

zoomIn = e => {
    e.preventDefault();
    ee.emit("zoomin");
}

zoomOut = e => {
    e.preventDefault();
    ee.emit("zoomout");
}

changeState = (e, state) => {
    e.preventDefault();
    ee.emit("statechange", state);
    this.setState({playerMode: state});
}

delete = e => {
    e.preventDefault();
    this.deleteRecord();
}

deleteRecord = () => {
    const that = this;
    if(totalTracks > 0){
        confirm({
            title: 'Delete Recording',
            content:
                'Are you sure want to delete the selected recording?',
            onOk() {
                ee.emit("delete");    
                totalTracks -= 1;
                tracksDeleted += 1;
                if(totalTracks == 0){
                    that.setState({isBlocked: false});
                }
            },
            onCancel() {},
            okText: 'Yes',
            cancelText: 'No'
        });
    }
 }

trimAudio = e => {
    e.preventDefault();
    ee.emit("trim");
}

slice = e => {
    e.preventDefault();
    ee.emit("slice");
}

switchTake = (e, take) => {
    e.preventDefault();
    let {currTakeIndx, takeAssigned, project} = this.state;
    const newTakeIndx = take === 'next' ? currTakeIndx + 1 : currTakeIndx - 1;
    const newTakeNo = takeAssigned[newTakeIndx] || -1;
    if(newTakeNo > 0 || newTakeNo <= project.takes.length){
        this.setState({currTakeIndx: newTakeIndx});
        this.changeTake(e, newTakeNo);
    }
}

getTakeStatus = (takeNo) => {
    const takesAttended = Object.assign([], this.state.takesAttended);
    const status = takesAttended.indexOf(takeNo) > -1 ? 'Attended': 'Un attended';
    return status;
}

clear() {
    const { canvasCtx } = this.state;
    this.canvasCtx.clearRect(0, 0, this.canvasCtx.canvas.width, this.canvasCtx.canvas.height);
    this.canvasCtx.fillRect(0, this.canvasCtx.canvas.height * (1 - 0.05), this.canvasCtx.canvas.width, this.canvasCtx.canvas.height);
}

undo = e => {
    e.preventDefault();
    ee.emit('undo');
}

 render() {
    let { me, project} = this.props;    
    const { isRecording, isPlaying, 
        takeNo, currTake,   
        character,  isBlocked, playerMode, isTrimDisabled} = this.state;
    let takeArr = false;
    let unattendedCount = 0;
      
    takeArr = project && project.takes ? [] : false;
    if(project.takes){
        for (var key in project.takes) {
            if (project.takes.hasOwnProperty(key)) {
                var val = project.takes[key];
                takeArr.push(val);
            }
        }
    }
    if(takeArr && takeArr.length > 0){
        takeArr.map((take, tKey) =>{
            const numOfSubtitles = take.filter((subtitle)=> subtitle.character && subtitle.character._id == charaId);            
            if(numOfSubtitles.length > 0){
                unattendedCount += this.getTakeStatus(take[take.length -1].take) == 'Un attended' ? 1 : 0;
            }
        });         
    }
    const projectTitle = project && project.project? `${project.project.title} (${character ? character.title : ''})`:''; 
   return (
     <LayoutContentWrapper className="studio-wrapper audiobook">
       <LayoutContent className="studio-layput">
       <Prompt
          when={isBlocked}
          message={location =>
            `You might lose the recordings. Are you sure you want to leave this page?`
          }
        />
       <div className="content-outer-wrp" ref="studio_panel">
		<div className="content-cntr clearfix">
			<section className="create-shift-cntr cnt-btm-pding">
				<section className="video-player-cntr studio-video-cntr">
					<div className="video-cntr">
                        <MovieInfo 
                            projectTitle={projectTitle} 
                            takeNo={takeNo}
                            me={me}
                            isRecording={isRecording}
                            isPlaying={isPlaying}
                            startRecording={this.startRecording}
                            stopRecording={this.stopRecording}
                            switchTake={this.switchTake}
                            play={this.play}
                        />
					</div>
					<div className="subtitle-cntr">
						<section className="sound-settings">
							<div className="sound-btns">
								<ul className="clearfix" style={{marginBottom:15}}>
                                    <li><a href="#" className={playerMode === 'cursor' ? 'active' : ''} onClick={(e)=>this.changeState(e, 'cursor')} title="Cursor"><i className="fa fa-headphones" ></i></a></li>
                                    <li><a href="#" className={playerMode === 'select' ? 'active' : ''} onClick={(e)=>this.changeState(e, 'select')} title="Select area"><i className="fa fa-italic" ></i></a></li>
                                    <li><a href="#" className={playerMode === 'shift' ? 'active' : ''} onClick={(e)=>this.changeState(e, 'shift')} title="Move clip"><i className="fa fa-arrows-h" ></i></a></li>
								</ul>	
							</div>
							<div className="zoom-wrp sound-btns clearfix">
								<ul className="left-cntr tool-icons">
                                    <li><a href="#" onClick={(e) => this.zoomIn(e)} title="Zoom In"><i className="fa fa-search-plus" ></i></a></li>
                                    <li><a href="#" onClick={(e) => this.zoomOut(e)} title="Zoom Out"><i className="fa fa-search-minus"></i></a></li>
                                    <li><a href="#" onClick={(e) => this.trimAudio(e)} title="Trim audio" disabled={isTrimDisabled}><i className="fa fa-crop"></i></a></li>
                                    <li><a href="#" onClick={(e) => this.slice(e)}title="Cut audio"><i className="fa fa-cut"></i></a></li>
                                    <li><a href="#" onClick={(e) => this.delete(e)} title="Delete audio"><i className="fa fa-trash"></i></a></li>
                                    <li><a href="#" onClick={(e) => this.submitWork(e, 'save')}title="Save recording"><i className="fa fa-save"></i></a></li>
                                    <li><a href="#" onClick={(e) => this.undo(e)}title="Undo last action"><i className="fa fa-undo"></i></a></li>
								</ul>
							</div>
						</section>
					</div>
				</section>
				<section className="dialogues-cntr studio-content-cntr">
					<div className="dialogues-list">
					<div className="scroll">
                    <Scrollbar className="transBoxScrollbar">
						<ul className="clearfix list-cntr">
							{currTake && currTake.length > 0 && currTake.map((take, idx) => <li key={idx}>
								<p>{take.character ?`${take.text}`:''}</p> 
                            </li>)}
						</ul>
                    </Scrollbar>
                    </div>						
					</div>
				</section>
				<section className="soundtrack">
                        <ul className="clearfix">
                            <li id="playlist" ref="playlist">
                            </li>				        
                        </ul>
				</section>
                <div id="takes" className="ant-table ant-table-large ant-table-scroll-position-left">
                <div className="ant-table-content">
                <div className="ant-table-body take-table">
                    <table className="">
                        <thead className="ant-table-thead">
                            <tr><th className=""><span><span>Take</span></span></th>
                                <th className=""><span><span>Status</span></span></th></tr>
                        </thead>
                        <tbody className="ant-table-tbody">
                            {takeArr && takeArr.length > 0 && takeArr.map((take, tKey) =>
                                <Take
                                    take={take}
                                    key={tKey}
                                    tKey={tKey}
                                    takeArr={takeArr}
                                    changeTake={this.changeTake}
                                    getTakeStatus={this.getTakeStatus} />
                            )}
                        </tbody>
                    </table>
                </div></div></div>
			</section>
		</div>
	</div>
       </LayoutContent>
     </LayoutContentWrapper>
   );
 }
}

const mapStateToProps = createStructuredSelector({
    project: getProjectDetails(),
    me: getProfile(),
  });
  
  function mapDispatchToProps(dispatch, ownProps) {
    return {
        fetchProjectInfo: (pId, cId) => dispatch(fetchProjectInfo(pId, cId)),
        getSavedDubTracks: (pId, cId) => dispatch(getSavedDubTracks(pId, cId)),
        submitFile: (fileArr, wavArr, data, savedRecordings, cb) => dispatch(submitFile(fileArr, wavArr, data, savedRecordings, cb)),
        saveRecording: (fileArr, wavArr, data, savedRecordings, cb) => dispatch(saveRecording(fileArr, wavArr, data, savedRecordings, cb)),
    };
  }
  export default connect(
    mapStateToProps,
    mapDispatchToProps
  )(Studio);